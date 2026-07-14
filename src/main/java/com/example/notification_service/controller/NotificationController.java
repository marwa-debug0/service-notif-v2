package com.example.notification_service.controller;
import com.example.notification_service.domain.NotificationLog;
import com.example.notification_service.repository.NotificationLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationLogRepository logRepository;

    public NotificationController(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<NotificationLog>> getLogs() {
        return ResponseEntity.ok(logRepository.findAll());
    }
}
