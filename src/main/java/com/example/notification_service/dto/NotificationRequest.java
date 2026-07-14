package com.example.notification_service.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequest {
    @NotBlank
    private String deviceToken;
    @NotBlank
    private String userId;
    @NotBlank
    private String title;
    @NotBlank
    private String body;
    private String notificationId;

    public NotificationRequest() {}

    public NotificationRequest(String deviceToken, String userId, String title, String body) {
        this.deviceToken = deviceToken;
        this.userId = userId;
        this.title = title;
        this.body = body;
    }

    public NotificationRequest(String deviceToken, String userId, String title, String body, String notificationId) {
        this.deviceToken = deviceToken;
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.notificationId = notificationId;
    }

    public String getDeviceToken() { 
        return deviceToken; 
    }
    
    public void setDeviceToken(String deviceToken) { 
        this.deviceToken = deviceToken; 
    }

    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }

    public String getTitle() { 
        return title; 
    }
    
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getBody() { 
        return body; 
    }
    
    public void setBody(String body) { 
        this.body = body; 
    }

    public String getNotificationId() { 
        return notificationId; 
    }
    
    public void setNotificationId(String notificationId) { 
        this.notificationId = notificationId; 
    }
}
