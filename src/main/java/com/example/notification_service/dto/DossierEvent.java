package com.example.notification_service.dto;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private Long dossierId;
    private Long clientId;
    private String dossierNumber;
    private String newStatus;
    private String title;
    private String message;
}
