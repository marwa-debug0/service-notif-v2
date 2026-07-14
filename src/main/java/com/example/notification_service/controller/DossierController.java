package com.example.notification_service.controller;

import com.example.notification_service.domain.Dossier;
import com.example.notification_service.dto.DossierDto;
import com.example.notification_service.service.DossierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    private final DossierService dossierService;

    public DossierController(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    @PostMapping
    public ResponseEntity<Dossier> createDossier(@Valid @RequestBody DossierDto dossierDto) {
        if (dossierDto.getClientId() == null) {
            throw new IllegalArgumentException("Client ID is required for dossier creation");
        }
        Dossier dossier = dossierService.createDossier(
                dossierDto.getDossierNumber(),
                dossierDto.getDescription(),
                dossierDto.getStatus(),
                dossierDto.getClientId()
        );
        return ResponseEntity.ok(dossier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dossier> updateDossier(@PathVariable Long id, @Valid @RequestBody DossierDto dossierDto) {
        Dossier dossier = dossierService.updateDossier(
                id,
                dossierDto.getDossierNumber(),
                dossierDto.getDescription(),
                dossierDto.getStatus()
        );
        return ResponseEntity.ok(dossier);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dossier> getDossier(@PathVariable Long id) {
        return dossierService.getDossier(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
