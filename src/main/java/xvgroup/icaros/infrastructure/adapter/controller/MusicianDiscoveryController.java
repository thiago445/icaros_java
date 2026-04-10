package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.UserProfileResponse;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.UserRepository;
import xvgroup.icaros.domain.service.MusicianDiscoveryService;

import java.util.List;
import java.util.UUID;

/**
 * MusicianDiscoveryController — permite ao PRODUTOR descobrir músicos.
 *
 * GET /musicians                → lista todos os músicos da plataforma
 * GET /musicians/genre/{genre}  → músicos filtrados por gênero
 * GET /musicians/{id}           → perfil completo de um músico
 */
@RestController
@RequestMapping("/musicians")
public class MusicianDiscoveryController {

    private final MusicianDiscoveryService discoveryService;

    public MusicianDiscoveryController(MusicianDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    // GET /musicians — todos os músicos (autenticado)
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAllMusicians(JwtAuthenticationToken token) {
        List<UserProfileResponse> musicians = discoveryService.getAllMusicians();
        return ResponseEntity.ok(musicians);
    }

    // GET /musicians/genre/{genre} — músicos por gênero (autenticado)
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<UserProfileResponse>> getMusiciansByGenre(@PathVariable String genre,
                                                                          JwtAuthenticationToken token) {
        List<UserProfileResponse> musicians = discoveryService.getMusiciansByGenre(genre);
        return ResponseEntity.ok(musicians);
    }

    // GET /musicians/{id} — perfil de um músico específico (público)
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getMusicianProfile(@PathVariable UUID id) {
        UserProfileResponse profile = discoveryService.getMusicianProfile(id);
        return ResponseEntity.ok(profile);
    }
}
