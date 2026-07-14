package com.example.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ClientDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String deviceToken;

    public ClientDto() {}

    public ClientDto(String name, String email, String deviceToken) {
        this.name = name;
        this.email = email;
        this.deviceToken = deviceToken;
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getDeviceToken() { 
        return deviceToken; 
    }
    
    public void setDeviceToken(String deviceToken) { 
        this.deviceToken = deviceToken; 
    }
}
