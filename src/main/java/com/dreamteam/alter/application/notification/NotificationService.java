package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFcmDeviceTokenQueryRepository;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.notification.type.NotificationType;
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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalTime;
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

    private static final int FCM_BATCH_SIZE = 500;

    private final FcmClient fcmClient;
    private final UserFcmDeviceTokenRepository userFCMDeviceTokenRepository;
    private final UserFcmDeviceTokenQueryRepository userFCMDeviceTokenQueryRepository;
    private final NotificationRepository notificationRepository;
    private final UserQueryRepository userQueryRepository;
    private final EntityManager entityManager;
    private final NotificationConsentQueryRepository notificationConsentQueryRepository;

    public void saveOrUpdateUserDeviceToken(User user, String deviceToken, DevicePlatformType devicePlatformType) {
        Optional<FcmDeviceToken> existingDeviceTokenByUser =
            userFCMDeviceTokenQueryRepository.findByUser(user);
        Optional<FcmDeviceToken> sameDeviceTokenEndpoint =
            userFCMDeviceTokenQueryRepository.findByDeviceToken(deviceToken);

        // deviceToken이 동일한 기존 매핑이 존재하고, 해당 매핑이 현재 사용자와 다를 경우 기존 매핑을 제거
        if (sameDeviceTokenEndpoint.isPresent() && !sameDeviceTokenEndpoint.get().getUser().equals(user)) {
            userFCMDeviceTokenRepository.delete(sameDeviceTokenEndpoint.get());
            entityManager.flush();
        }

        // userId에 매핑된 기존 엔드포인트가 없는 경우 새로 생성
        if (existingDeviceTokenByUser.isEmpty()) {
            userFCMDeviceTokenRepository.save(FcmDeviceToken.create(
                user,
                deviceToken,
                devicePlatformType
            ));
            return;
        }

        FcmDeviceToken userEndpoint = existingDeviceTokenByUser.get();
        String oldDeviceToken = userEndpoint.getDeviceToken();

        // 디바이스 토큰이 변경된 경우에만 업데이트 수행
        if (!oldDeviceToken.equals(deviceToken) || !userEndpoint.getDevicePlatform().equals(devicePlatformType)) {
            userEndpoint.updateDeviceToken(deviceToken, devicePlatformType);
        }
    }

    public void removeUserDeviceToken(User user) {
        Optional<FcmDeviceToken> deviceTokenOpt = userFCMDeviceTokenQueryRepository.findByUser(user);

        deviceTokenOpt.ifPresent(userFCMDeviceTokenRepository::delete);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotificationAfterCommit(FcmNotificationRequestDto request) {
        sendNotification(request, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendMultipleNotificationsAfterCommit(FcmBatchNotificationRequestDto request) {
        sendMultipleNotifications(request, true);
    }

    public void sendNotification(FcmNotificationRequestDto request) {
        sendNotification(request, false);
    }

    private void sendNotification(FcmNotificationRequestDto request, boolean preserveOnFcmFailure) {
        // 1. 사용자 조회
        User user = userQueryRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 디바이스 토큰 조회
        Optional<FcmDeviceToken> deviceTokenOpt = userFCMDeviceTokenQueryRepository.findByUser(user);
        String deviceTokenString = deviceTokenOpt.map(FcmDeviceToken::getDeviceToken).orElse(null);

        // 3. 알림 레코드 저장
        saveNotification(user, request.getScope(), request.getType(), deviceTokenString, request.getTitle(), request.getBody());

        // 4. FCM 발송
        sendFcmNotification(user, deviceTokenOpt, request.getType(), request.getTitle(), request.getBody(), preserveOnFcmFailure);
    }

    public void sendMultipleNotifications(FcmBatchNotificationRequestDto request) {
        sendMultipleNotifications(request, false);
    }

    private void sendMultipleNotifications(FcmBatchNotificationRequestDto request, boolean preserveOnFcmFailure) {
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
                request.getType(),
                deviceTokenMap.get(user.getId()),
                request.getTitle(),
                request.getBody()
            ))
            .toList();
        notificationRepository.saveAll(notifications);

        // 4. 수신 동의를 통과한 토큰을 FCM 제한에 맞춰 발송
        List<FcmDeviceToken> eligibleTokens = filterEligibleTokens(users, deviceTokens, request.getType());
        dispatchBatch(eligibleTokens, request.getTitle(), request.getBody(), !preserveOnFcmFailure, preserveOnFcmFailure);
    }

    /**
     * 배치 응답 처리
     */
    private void processBatchResponse(
        BatchResponse response, List<FcmDeviceToken> deviceTokens, List<FcmDeviceToken> invalidTokens
    ) {
        List<SendResponse> responses = response.getResponses();
        int responseSize = responses.size();
        for (int i = 0; i < responseSize; i++) {
            SendResponse sendResponse = responses.get(i);
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
    }

    /**
     * 여러 사용자에게 FCM 알림만 배치 발송 (Notification 엔티티 저장 없음)
     * 채팅 그룹 메시지처럼 멤버별 발송을 한 번의 토큰 조회 + FCM 배치로 처리할 때 사용.
     */
    public void sendNotificationOnlyToMany(List<Long> userIds, NotificationType type, String title, String body) {
        if (ObjectUtils.isEmpty(userIds)) {
            return;
        }

        // 1. 사용자 배치 조회
        List<User> users = userQueryRepository.findAllById(userIds);
        if (ObjectUtils.isEmpty(users)) {
            return;
        }

        // 2. 디바이스 토큰 배치 조회
        List<FcmDeviceToken> deviceTokens = userFCMDeviceTokenQueryRepository.findByUsers(users);
        if (ObjectUtils.isEmpty(deviceTokens)) {
            return;
        }

        // 3. 채팅은 기존과 같이 FirebaseMessagingException만 기록하고 계속 발송한다.
        List<FcmDeviceToken> eligibleTokens = filterEligibleTokens(users, deviceTokens, type);
        dispatchBatch(eligibleTokens, title, body, false, false);
    }

    /**
     * 사용자·디바이스 토큰 목록에서 수신 동의(시간대 포함)를 통과한 토큰만 필터링.
     */
    private List<FcmDeviceToken> filterEligibleTokens(
        List<User> users, List<FcmDeviceToken> deviceTokens, NotificationType type
    ) {
        Map<Long, NotificationConsent> consentMap = notificationConsentQueryRepository.findByUsers(users)
            .stream()
            .collect(Collectors.toMap(c -> c.getUser().getId(), c -> c));

        boolean daytime = isDaytime();
        return deviceTokens.stream()
            .filter(dt -> {
                NotificationConsent consent = consentMap.get(dt.getUser().getId());
                if (consent == null) {
                    log.warn("알림 수신 동의 레코드가 없어 발송을 스킵합니다. userId={}", dt.getUser().getId());
                    return false;
                }
                return !consent.isBlocked(type, daytime);
            })
            .toList();
    }

    /**
     * 대상 토큰들에 FCM 배치 발송 후 결과 처리.
     * 응답은 각 배치의 토큰 순서에 대응하며 무효 토큰은 전체 발송 후 한 번에 삭제한다.
     * @param throwOnError FirebaseMessagingException을 기존 CustomException으로 전파할지 여부
     * @param preserveOnFcmFailure 커밋 후 지원 알림에서 FCM 호출의 런타임 실패까지 기록하고 계속할지 여부
     */
    private void dispatchBatch(
        List<FcmDeviceToken> eligibleTokens, String title, String body,
        boolean throwOnError, boolean preserveOnFcmFailure
    ) {
        if (eligibleTokens.isEmpty()) {
            return;
        }

        List<FcmDeviceToken> invalidTokens = new ArrayList<>();
        for (int start = 0; start < eligibleTokens.size(); start += FCM_BATCH_SIZE) {
            List<FcmDeviceToken> batch = eligibleTokens.subList(start, Math.min(start + FCM_BATCH_SIZE, eligibleTokens.size()));
            List<String> deviceTokenStrings = batch.stream()
                .map(FcmDeviceToken::getDeviceToken)
                .toList();
            BatchResponse response;
            try {
                response = fcmClient.sendMultipleNotifications(deviceTokenStrings, title, body);
            } catch (FirebaseMessagingException | RuntimeException e) {
                log.error("FCM 배치 알림 발송 실패: {}", e.getMessage(), e);
                rethrowFcmFailure(e, throwOnError, preserveOnFcmFailure, "FCM 다중 알림 발송 실패");
                continue;
            }
            // DB 변경과 응답 처리는 FCM 호출의 예외 처리 범위 밖에서 수행한다.
            processBatchResponse(response, batch, invalidTokens);
        }
        if (!invalidTokens.isEmpty()) {
            userFCMDeviceTokenRepository.deleteAll(invalidTokens);
        }
    }

    /**
     * FCM 알림 발송 내부 로직 (공통)
     * @param user 대상 사용자
     * @param title 알림 제목
     * @param body 알림 본문
     */
    private void sendFcmNotification(
        User user, Optional<FcmDeviceToken> deviceTokenOpt, NotificationType type,
        String title, String body, boolean preserveOnFcmFailure
    ) {
        if (isNotificationBlocked(user, type)) {
            log.debug("알림 수신 동의 거부로 FCM 발송 건너뜀. userId={}", user.getId());
            return;
        }

        if (deviceTokenOpt.isEmpty()) {
            log.warn("사용자 {}의 디바이스 토큰이 없습니다.", user.getId());
            return;
        }

        FcmDeviceToken deviceToken = deviceTokenOpt.get();

        try {
            // FCM 알림 전송
            fcmClient.sendNotification(deviceToken.getDeviceToken(), title, body);
        } catch (FirebaseMessagingException | RuntimeException e) {
            log.error("FCM 알림 발송 실패. UserId: {}, Error: {}", user.getId(), e.getMessage(), e);

            // 토큰 무효화 처리
            if (e instanceof FirebaseMessagingException firebaseException && isTokenInvalid(firebaseException)) {
                userFCMDeviceTokenRepository.delete(deviceToken);
            }
            rethrowFcmFailure(e, !preserveOnFcmFailure, preserveOnFcmFailure, "FCM 알림 발송 실패");
            return;
        }
        deviceToken.updateLastNotificationSentAt();
    }

    private void rethrowFcmFailure(
        Exception failure, boolean throwOnError, boolean preserveOnFcmFailure, String message
    ) {
        if (preserveOnFcmFailure) {
            return;
        }
        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwOnError) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, message);
        }
    }

    /**
     * Notification 엔티티 저장
     */
    private void saveNotification(
        User user, TokenScope scope, NotificationType type, String deviceToken, String title, String body
    ) {
        notificationRepository.save(Notification.create(
            user, scope, type, deviceToken, title, body
        ));
    }

    /**
     * FCM 토큰이 무효한지 확인
     */
    private boolean isTokenInvalid(FirebaseMessagingException e) {
        return MessagingErrorCode.INVALID_ARGUMENT.equals(e.getMessagingErrorCode()) ||
            MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode());
    }


    private boolean isNotificationBlocked(User user, NotificationType type) {
        Optional<NotificationConsent> consentOpt = notificationConsentQueryRepository.findByUser(user);
        if (consentOpt.isEmpty()) {
            log.warn("알림 수신 동의 레코드가 없어 발송을 스킵합니다. userId={}", user.getId());
            return true;
        }
        return consentOpt.get().isBlocked(type, isDaytime());
    }

    private boolean isDaytime() {
        int hour = LocalTime.now().getHour();
        return hour >= 8 && hour < 21;
    }
}
