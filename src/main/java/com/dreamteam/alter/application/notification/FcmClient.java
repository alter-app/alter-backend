package com.dreamteam.alter.application.notification;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmClient {

    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(String deviceToken, String title, String body)
        throws FirebaseMessagingException
    {
        Message message = createMessage(deviceToken, title, body);
        firebaseMessaging.send(message);
    }

    public BatchResponse sendMultipleNotifications(List<String> deviceTokens, String title, String body)
        throws FirebaseMessagingException
    {
        MulticastMessage message = createMulticastMessage(deviceTokens, title, body);
        return firebaseMessaging.sendEachForMulticast(message);
    }

    private Message createMessage(String deviceToken, String title, String body) {
        return Message.builder()
            .setToken(deviceToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(
                        AndroidNotification.builder()
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .setChannelId("high_priority_channel")
                            .setSound("default")
                            .setDefaultSound(true)
                            .setDefaultVibrateTimings(true)
                            .build()
                    )
                    .build()
            )
            .setApnsConfig(
                ApnsConfig.builder()
                    .setAps(
                        Aps.builder()
                            .setAlert(
                                ApsAlert.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                            )
                            .setSound("default")
                            .setBadge(1)
                            .setContentAvailable(true)
                            .setMutableContent(true)
                            .build()
                    )
                    .putHeader("apns-priority", "10")
                    .putHeader("apns-push-type", "alert")
                    .build()
            )
            .build();
    }

    private MulticastMessage createMulticastMessage(List<String> deviceTokens, String title, String body) {
        return MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(
                        AndroidNotification.builder()
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .setChannelId("high_priority_channel")
                            .setSound("default")
                            .setDefaultSound(true)
                            .setDefaultVibrateTimings(true)
                            .build()
                    )
                    .build()
            )
            .setApnsConfig(
                ApnsConfig.builder()
                    .setAps(
                        Aps.builder()
                            .setAlert(
                                ApsAlert.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                            )
                            .setSound("default")
                            .setBadge(1)
                            .setContentAvailable(true)
                            .setMutableContent(true)
                            .build()
                    )
                    .putHeader("apns-priority", "10")
                    .putHeader("apns-push-type", "alert")
                    .build()
            )
            .build();
    }

}
