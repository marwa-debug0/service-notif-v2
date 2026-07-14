package com.example.notification_service.controller;

import com.example.notification_service.domain.Client;
import com.example.notification_service.dto.ClientDto;
import com.example.notification_service.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@Valid @RequestBody ClientDto clientDto) {
        Client client = Client.builder()
                .name(clientDto.getName())
                .email(clientDto.getEmail())
                .deviceToken(clientDto.getDeviceToken())
                .build();
        Client saved = clientRepository.save(client);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }

    @PutMapping("/{id}/token")
    public ResponseEntity<Client> updateDeviceToken(@PathVariable Long id, @RequestParam String token) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + id));
        client.setDeviceToken(token);
        Client saved = clientRepository.save(client);
        return ResponseEntity.ok(saved);
    }
}
