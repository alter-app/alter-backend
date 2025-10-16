package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFcmDeviceTokenQueryRepository;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserFcmDeviceTokenRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.DevicePlatformType;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final FcmClient fcmClient;
    private final UserFcmDeviceTokenRepository userFCMDeviceTokenRepository;
    private final UserFcmDeviceTokenQueryRepository userFCMDeviceTokenQueryRepository;
    private final NotificationRepository notificationRepository;
    private final UserQueryRepository userQueryRepository;

    public void saveOrUpdateUserDeviceToken(User user, String deviceToken, DevicePlatformType devicePlatformType) {
        Optional<FcmDeviceToken> existingDeviceTokenByUser =
            userFCMDeviceTokenQueryRepository.findByUser(user);

        if (existingDeviceTokenByUser.isPresent()) {
            existingDeviceTokenByUser.get()
                .updateDeviceToken(deviceToken, devicePlatformType);
        } else {
            userFCMDeviceTokenQueryRepository.findByDeviceToken(deviceToken)
                .ifPresent(userFCMDeviceTokenRepository::delete);

            userFCMDeviceTokenRepository.save(FcmDeviceToken.create(
                user,
                deviceToken,
                devicePlatformType
            ));
        }
    }

    public void sendNotification(FcmNotificationRequestDto request) {
        // 1. 사용자 조회
        User user = userQueryRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 디바이스 토큰 조회
        Optional<FcmDeviceToken> deviceTokenOpt = userFCMDeviceTokenQueryRepository.findByUser(user);
        if (deviceTokenOpt.isEmpty()) {
            log.warn("사용자 {}의 디바이스 토큰이 없습니다.", request.getTargetUserId());
            return;
        }

        FcmDeviceToken deviceToken = deviceTokenOpt.get();

        try {
            // 3. 알림 레코드 저장
            Notification notification = Notification.create(
                user, deviceToken.getDeviceToken(), request.getTitle(), request.getBody()
            );
            notificationRepository.save(notification);

            // 4. FCM 발송
            fcmClient.sendNotification(deviceToken.getDeviceToken(), request.getTitle(), request.getBody());
            deviceToken.updateLastNotificationSentAt();

        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 발송 실패. UserId: {}, Error: {}", deviceToken.getUser().getId(), e.getMessage(), e);

            // 토큰 무효화 처리
            if (isTokenInvalid(e)) {
                userFCMDeviceTokenRepository.delete(deviceToken);
            }

            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "FCM 알림 발송 실패");
        }
    }

    public void sendMultipleNotifications(FcmBatchNotificationRequestDto request) {
        // 1. 사용자들 조회
        List<User> users = userQueryRepository.findAllById(request.getTargetUserIds());

        // 2. 각 사용자의 디바이스 토큰 조회
        List<FcmDeviceToken> deviceTokens = users.stream()
            .map(userFCMDeviceTokenQueryRepository::findByUser)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        if (deviceTokens.isEmpty()) {
            log.warn("발송할 디바이스 토큰이 없습니다.");
            return;
        }

        try {
            // 3. 알림 레코드 배치 저장
            List<Notification> notifications = deviceTokens.stream()
                .map(deviceToken -> Notification.create(
                    deviceToken.getUser(),
                    deviceToken.getDeviceToken(),
                    request.getTitle(),
                    request.getBody()
                ))
                .toList();
            notificationRepository.saveAll(notifications);

            // 4. FCM 배치 발송
            List<String> deviceTokenStrings = deviceTokens.stream()
                .map(FcmDeviceToken::getDeviceToken)
                .toList();

            BatchResponse response = fcmClient.sendMultipleNotifications(
                deviceTokenStrings, request.getTitle(), request.getBody()
            );

            // 5. 결과 처리
            processBatchResponse(response, deviceTokens);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 다중 알림 발송 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "FCM 다중 알림 발송 실패");
        }
    }

    /**
     * 배치 응답 처리
     */
    private void processBatchResponse(BatchResponse response, List<FcmDeviceToken> deviceTokens) {
        List<FcmDeviceToken> invalidTokens = new ArrayList<>();

        int responseSize = response.getResponses().size();
        for (int i = 0; i < responseSize; i++) {
            SendResponse sendResponse = response.getResponses()
                .get(i);
            FcmDeviceToken deviceToken = deviceTokens.get(i);

            if (sendResponse.isSuccessful()) {
                deviceToken.updateLastNotificationSentAt();
            } else {
                FirebaseMessagingException exception = sendResponse.getException();
                log.error(
                    "FCM 알림 발송 실패. UserId: {}, Error Code: {}, Error Message: {}",
                    deviceToken.getUser().getId(),
                    exception.getMessagingErrorCode(),
                    exception.getMessage()
                );

                if (isTokenInvalid(exception)) {
                    invalidTokens.add(deviceToken);
                }
            }
        }

        // 무효한 토큰들 일괄 삭제
        if (!invalidTokens.isEmpty()) {
            userFCMDeviceTokenRepository.deleteAll(invalidTokens);
        }
    }

    /**
     * FCM 토큰이 무효한지 확인
     */
    private boolean isTokenInvalid(FirebaseMessagingException e) {
        return MessagingErrorCode.INVALID_ARGUMENT.equals(e.getMessagingErrorCode()) ||
            MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode());
    }

}
