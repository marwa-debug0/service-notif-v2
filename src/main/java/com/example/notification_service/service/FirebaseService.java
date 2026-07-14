package com.example.notification_service.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.io.IOException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;

@Service
public class FirebaseService {
    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);

    private final ResourceLoader resourceLoader;

    @Value("${app.firebase.config-path:classpath:firebase-service-account.json}")
    private String firebaseConfigPath;

    @Value("${app.firebase.mock-mode:false}")
    private boolean mockMode;

    private boolean firebaseInitialized = false;

    public FirebaseService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        if (mockMode) {
            log.warn("Firebase initialized in MOCK MODE. Notifications will be logged instead of sent to Google.");
            return;
        }

        try {
            Resource resource = resourceLoader.getResource(firebaseConfigPath);
            if (!resource.exists()) {
                log.warn("Firebase service account JSON file not found at: {}. Falling back to MOCK MODE.", firebaseConfigPath);
                this.mockMode = true;
                return;
            }
            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                firebaseInitialized = true;
                log.info("Firebase Application has been initialized successfully.");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase SDK: {}. Falling back to MOCK MODE.", e.getMessage());
            this.mockMode = true;
        }
    }

    public String sendPushNotification(String deviceToken, String title, String body) throws Exception {
        if (mockMode || !firebaseInitialized || deviceToken == null || deviceToken.startsWith("mock-")) {
            log.info("[MOCK FCM] Sending push notification to token '{}': [{}] {}", deviceToken, title, body);
            return "mock-message-id-" + java.util.UUID.randomUUID().toString();
        }

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }
}
