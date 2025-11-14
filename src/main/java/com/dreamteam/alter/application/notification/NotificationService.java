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
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.DevicePlatformType;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            existingDeviceTokenByUser.get().updateDeviceToken(deviceToken, devicePlatformType);
            return;
        }

        Optional<FcmDeviceToken> existingDeviceToken =
            userFCMDeviceTokenQueryRepository.findByDeviceToken(deviceToken);

        if (existingDeviceToken.isPresent()) {
            existingDeviceToken.get().updateUserAndDeviceToken(user, deviceToken, devicePlatformType);
            return;
        }

        userFCMDeviceTokenRepository.save(FcmDeviceToken.create(
            user,
            deviceToken,
            devicePlatformType
        ));
    }

    public void sendNotification(FcmNotificationRequestDto request) {
        // 1. 사용자 조회
        User user = userQueryRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 디바이스 토큰 조회
        Optional<FcmDeviceToken> deviceTokenOpt = userFCMDeviceTokenQueryRepository.findByUser(user);
        String deviceTokenString = deviceTokenOpt.map(FcmDeviceToken::getDeviceToken).orElse(null);

        // 3. 알림 레코드 저장
        saveNotification(user, request.getScope(), deviceTokenString, request.getTitle(), request.getBody());

        // 4. FCM 발송
        sendFcmNotification(user, request.getTitle(), request.getBody(), true);
    }

    public void sendMultipleNotifications(FcmBatchNotificationRequestDto request) {
        // 1. 사용자들 조회
        List<User> users = userQueryRepository.findAllById(request.getTargetUserIds());

        if (ObjectUtils.isEmpty(users)) {
            log.warn("발송할 사용자가 없습니다.");
            return;
        }

        // 2. 배치로 디바이스 토큰 조회
        List<FcmDeviceToken> deviceTokens = userFCMDeviceTokenQueryRepository.findByUsers(users);

        // 사용자에 매핑된 DeviceToken들을 Map으로 변환
        Map<Long, String> deviceTokenMap = deviceTokens.stream()
            .collect(Collectors.toMap(
                dt -> dt.getUser().getId(),
                FcmDeviceToken::getDeviceToken
            ));

        // 3. 모든 사용자에 대해 알림 레코드 저장
        List<Notification> notifications = users.stream()
            .map(user -> Notification.create(
                user,
                request.getScope(),
                deviceTokenMap.get(user.getId()),
                request.getTitle(),
                request.getBody()
            ))
            .toList();
        notificationRepository.saveAll(notifications);

        // 4. FCM 배치 발송
        if (deviceTokens.isEmpty()) {
            return;
        }

        try {
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
     * FCM 알림만 발송 (Notification 엔티티 저장 없음)
     * 채팅 메시지 등 별도 저장 로직이 있는 경우 사용
     */
    public void sendNotificationOnly(Long userId, String title, String body) {
        User user = userQueryRepository.findById(userId)
            .orElse(null);

        if (ObjectUtils.isEmpty(user)) {
            log.warn("사용자를 찾을 수 없습니다. UserId: {}", userId);
            return;
        }

        sendFcmNotification(user, title, body, false);
    }

    /**
     * FCM 알림 발송 내부 로직 (공통)
     * @param user 대상 사용자
     * @param title 알림 제목
     * @param body 알림 본문
     * @param throwOnError 발송 실패 시 예외 throw 여부
     */
    private void sendFcmNotification(User user, String title, String body, boolean throwOnError) {
        // 디바이스 토큰 조회
        Optional<FcmDeviceToken> deviceTokenOpt = userFCMDeviceTokenQueryRepository.findByUser(user);
        if (deviceTokenOpt.isEmpty()) {
            log.warn("사용자 {}의 디바이스 토큰이 없습니다.", user.getId());
            return;
        }

        FcmDeviceToken deviceToken = deviceTokenOpt.get();

        try {
            // FCM 알림 전송
            fcmClient.sendNotification(deviceToken.getDeviceToken(), title, body);
            deviceToken.updateLastNotificationSentAt();

        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 발송 실패. UserId: {}, Error: {}", user.getId(), e.getMessage(), e);

            // 토큰 무효화 처리
            if (isTokenInvalid(e)) {
                userFCMDeviceTokenRepository.delete(deviceToken);
            }

            if (throwOnError) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "FCM 알림 발송 실패");
            }
        }
    }

    /**
     * Notification 엔티티 저장
     */
    private void saveNotification(User user, TokenScope scope, String deviceToken, String title, String body) {
        notificationRepository.save(Notification.create(
            user, scope, deviceToken, title, body
        ));
    }

    /**
     * FCM 토큰이 무효한지 확인
     */
    private boolean isTokenInvalid(FirebaseMessagingException e) {
        return MessagingErrorCode.INVALID_ARGUMENT.equals(e.getMessagingErrorCode()) ||
            MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode());
    }

}
