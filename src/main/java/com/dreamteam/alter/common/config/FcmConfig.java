package com.dreamteam.alter.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.fcm.service-account-key}")
    private String serviceAccountKey;

    @Value("${firebase.fcm.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                byte[] decodedKey = Base64.getDecoder()
                    .decode(serviceAccountKey);

                GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(decodedKey)
                );

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Successfully Initialized Firebase Admin SDK");
                return app;
            } else {
                return FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            log.error("Error Occurred While Initializing Firebase Admin SDK : {}", e.getMessage(), e);
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

}
