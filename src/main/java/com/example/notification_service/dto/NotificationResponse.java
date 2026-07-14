package com.example.notification_service.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private String notificationId;
    private String status;
    private LocalDateTime timestamp;
    private String message;

    public NotificationResponse() {
    }

    public NotificationResponse(String notificationId, String status, LocalDateTime timestamp, String message) {
        this.notificationId = notificationId;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String notificationId;
        private String status;
        private LocalDateTime timestamp;
        private String message;

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationResponse build() {
            return new NotificationResponse(notificationId, status, timestamp, message);
        }
    }
}
