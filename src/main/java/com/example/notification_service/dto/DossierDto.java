package com.example.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierDto {
    @NotBlank(message = "Dossier number is required")
    private String dossierNumber;

    private String description;

    @NotBlank(message = "Status is required")
    private String status;

    private Long clientId;
}
