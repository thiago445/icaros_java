package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.GenreRankingResponse;
import xvgroup.icaros.domain.service.GenreRankingService;

import java.util.List;

/**
 * GenreRankingController — ranking de gêneros musicais da plataforma.
 *
 * GET    /ranking/genres          → Retorna ranking atual (público)
 * POST   /ranking/genres/refresh  → Recalcula o ranking (autenticado)
 *
 * Fórmula: score = likes×1.0 + comentários×2.0 + posts×0.5 + músicos×3.0
 */
@RestController
@RequestMapping("/ranking")
public class GenreRankingController {

    private final GenreRankingService rankingService;

    public GenreRankingController(GenreRankingService rankingService) {
        this.rankingService = rankingService;
    }

    // GET /ranking/genres — ranking atual (público, sem JWT)
    @GetMapping("/genres")
    public ResponseEntity<List<GenreRankingResponse>> getRanking() {
        List<GenreRankingResponse> ranking = rankingService.getRanking();
        return ResponseEntity.ok(ranking);
    }

    // POST /ranking/genres/refresh — recalcula (requer JWT, qualquer usuário)
    @PostMapping("/genres/refresh")
    public ResponseEntity<List<GenreRankingResponse>> refreshRanking(JwtAuthenticationToken token) {
        List<GenreRankingResponse> ranking = rankingService.refreshRanking();
        return ResponseEntity.ok(ranking);
    }
}
