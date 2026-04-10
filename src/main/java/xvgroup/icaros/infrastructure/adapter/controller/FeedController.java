package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xvgroup.icaros.application.dto.FeedPage;
import xvgroup.icaros.domain.service.FeedService;

/**
 * FeedController — endpoints do feed inteligente da plataforma Icaros.
 *
 * GET /feed           → Feed personalizado com scoring (requer autenticação)
 * GET /feed/public    → Feed público sem scoring (sem autenticação)
 *
 * Parâmetros de paginação:
 *   page (default 0)  → número da página
 *   size (default 20) → posts por página
 */
@RestController
@RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /**
     * Feed personalizado — requer JWT.
     *
     * O algoritmo:
     *   1. Filtra posts por role do usuário (produtor/ouvinte vê só músicos)
     *   2. Calcula score de cada post (gênero + engajamento + recência)
     *   3. Ordena pelo score decrescente com Java Streams
     *   4. Retorna página com metadados de paginação
     *
     * Exemplo: GET /feed?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<FeedPage> getPersonalizedFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            JwtAuthenticationToken token) {

        FeedPage feedPage = feedService.getPersonalizedFeed(page, size, token);
        return ResponseEntity.ok(feedPage);
    }

    /**
     * Feed público — sem autenticação, sem scoring.
     * Ordenação simples por data de publicação decrescente.
     *
     * Exemplo: GET /feed/public?page=0&size=10
     */
    @GetMapping("/public")
    public ResponseEntity<FeedPage> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FeedPage feedPage = feedService.getPublicFeed(page, size);
        return ResponseEntity.ok(feedPage);
    }
}
