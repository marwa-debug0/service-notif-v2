package com.example.notification_service.service;

import com.example.notification_service.config.RabbitMQconfig;
import com.example.notification_service.dto.DossierEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationProducer {
    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void queueNotification(DossierEvent event) {
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            event.setEventId(UUID.randomUUID().toString());
        }
        
        log.info("Queueing dossier event for client: {} with ID: {} to RabbitMQ", event.getClientId(), event.getEventId());
        
        rabbitTemplate.convertAndSend(
                RabbitMQconfig.EXCHANGE,
                RabbitMQconfig.MAIN_ROUTING_KEY,
                event
        );
        
        log.info("Dossier event successfully sent to exchange: {} with routing key: {} for ID: {}", 
                RabbitMQconfig.EXCHANGE, RabbitMQconfig.MAIN_ROUTING_KEY, event.getEventId());
    }
}
