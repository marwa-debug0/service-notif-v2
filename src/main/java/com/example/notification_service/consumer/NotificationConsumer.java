package com.example.notification_service.consumer;

import com.example.notification_service.config.RabbitMQconfig;
import com.example.notification_service.domain.Client;
import com.example.notification_service.domain.NotificationLog;
import com.example.notification_service.domain.NotificationStatus;
import com.example.notification_service.dto.DossierEvent;
import com.example.notification_service.repository.ClientRepository;
import com.example.notification_service.repository.NotificationLogRepository;
import com.example.notification_service.service.FirebaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final FirebaseService firebaseService;
    private final NotificationLogRepository logRepository;
    private final ClientRepository clientRepository;

    public NotificationConsumer(FirebaseService firebaseService, 
                                NotificationLogRepository logRepository,
                                ClientRepository clientRepository) {
        this.firebaseService = firebaseService;
        this.logRepository = logRepository;
        this.clientRepository = clientRepository;
    }

    @RabbitListener(queues = RabbitMQconfig.MAIN_QUEUE)
    public void consumeNotification(DossierEvent event) {
        log.info("Consuming dossier event from queue: ID={}, ClientID={}, Status={}", 
                event.getEventId(), event.getClientId(), event.getNewStatus());

        NotificationLog dbLog = logRepository.findById(event.getEventId())
                .orElse(null);

        if (dbLog == null) {
            // New notification attempt
            dbLog = NotificationLog.builder()
                    .notificationId(event.getEventId())
                    .userId(event.getClientId().toString())
                    .title(event.getTitle())
                    .body(event.getMessage())
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .timestamp(LocalDateTime.now())
                    .build();
            logRepository.save(dbLog);
        } else {
            // It's a retry attempt
            if (dbLog.getStatus() == NotificationStatus.SENT) {
                log.info("Notification with ID: {} was already sent successfully. Skipping duplicate processing.", 
                        event.getEventId());
                return;
            }
            dbLog.setRetryCount(dbLog.getRetryCount() + 1);
            dbLog.setTimestamp(LocalDateTime.now());
            logRepository.save(dbLog);
        }

        try {
            // Fetch active Client device token from the database
            Client client = clientRepository.findById(event.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + event.getClientId()));

            String deviceToken = client.getDeviceToken();
            if (deviceToken == null || deviceToken.trim().isEmpty()) {
                throw new IllegalStateException("FCM Device token is empty/null for Client ID: " + client.getId());
            }

            // Call Firebase Cloud Messaging SDK
            String fcmMessageId = firebaseService.sendPushNotification(
                    deviceToken,
                    event.getTitle(),
                    event.getMessage()
            );

            log.info("FCM push notification successfully delivered. Message ID: {}", fcmMessageId);

            dbLog.setStatus(NotificationStatus.SENT);
            dbLog.setErrorMessage(null);
            logRepository.save(dbLog);

        } catch (Exception e) {
            log.error("Failed to deliver FCM push notification for event ID: {}. Error: {}", 
                    event.getEventId(), e.getMessage());

            dbLog.setStatus(NotificationStatus.FAILED);
            dbLog.setErrorMessage(e.getMessage());
            logRepository.save(dbLog);

            if (dbLog.getRetryCount() < 3) {
                log.info("Retrying notification delivery (retry count: {}/3). Re-throwing exception to trigger RabbitMQ DLX delay.", 
                        dbLog.getRetryCount());
                throw new RuntimeException("FCM delivery failed, scheduling retry: " + e.getMessage(), e);
            } else {
                log.error("Notification delivery job ID: {} failed 3 times. Discarding message.", event.getEventId());
            }
        }
    }
}
