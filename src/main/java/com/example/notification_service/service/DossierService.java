package com.example.notification_service.service;

import com.example.notification_service.domain.Client;
import com.example.notification_service.domain.Dossier;
import com.example.notification_service.dto.DossierEvent;
import com.example.notification_service.repository.ClientRepository;
import com.example.notification_service.repository.DossierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class DossierService {
    private static final Logger log = LoggerFactory.getLogger(DossierService.class);

    private final DossierRepository dossierRepository;
    private final ClientRepository clientRepository;
    private final NotificationProducer notificationProducer;

    public DossierService(DossierRepository dossierRepository, 
                          ClientRepository clientRepository, 
                          NotificationProducer notificationProducer) {
        this.dossierRepository = dossierRepository;
        this.clientRepository = clientRepository;
        this.notificationProducer = notificationProducer;
    }

    @Transactional
    public Dossier createDossier(String dossierNumber, String description, String status, Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + clientId));

        Dossier dossier = Dossier.builder()
                .dossierNumber(dossierNumber)
                .description(description)
                .status(status)
                .client(client)
                .build();

        return dossierRepository.save(dossier);
    }

    @Transactional
    public Dossier updateDossier(Long dossierId, String dossierNumber, String description, String status) {
        Dossier existingDossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier not found with id: " + dossierId));

        boolean isChanged = !Objects.equals(existingDossier.getDossierNumber(), dossierNumber)
                || !Objects.equals(existingDossier.getDescription(), description)
                || !Objects.equals(existingDossier.getStatus(), status);

        if (isChanged) {
            log.info("Change detected in dossier id: {}. Old status: {}, New status: {}", 
                    dossierId, existingDossier.getStatus(), status);
            
            existingDossier.setDossierNumber(dossierNumber);
            existingDossier.setDescription(description);
            existingDossier.setStatus(status);
            
            Dossier savedDossier = dossierRepository.save(existingDossier);

            Client client = savedDossier.getClient();
            if (client != null) {
                String eventId = UUID.randomUUID().toString();
                String title = "Changement d'état du dossier";
                String message = String.format("Cher %s, l'état de votre dossier assurance '%s' a été modifié par l'administrateur. Nouveau statut : %s.", 
                        client.getName(), savedDossier.getDossierNumber(), savedDossier.getStatus());

                DossierEvent dossierEvent = new DossierEvent(
                        eventId,
                        savedDossier.getId(),
                        client.getId(),
                        savedDossier.getDossierNumber(),
                        savedDossier.getStatus(),
                        title,
                        message
                );

                // Publish raw DossierEvent to RabbitMQ
                notificationProducer.queueNotification(dossierEvent);
                log.info("Dossier event published to RabbitMQ for client {}", client.getId());
            }
            return savedDossier;
        } else {
            log.info("No changes detected in dossier id: {}. Skipping notification.", dossierId);
            return existingDossier;
        }
    }

    public Optional<Dossier> getDossier(Long id) {
        return dossierRepository.findById(id);
    }
}
