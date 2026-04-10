package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xvgroup.icaros.application.dto.ListenerRegisterRequest;
import xvgroup.icaros.application.dto.UserProfileResponse;
import xvgroup.icaros.domain.service.ListenerService;

/**
 * ListenerController — cadastro de ouvintes (lovers).
 *
 * POST /register/listener
 *   Body:
 *   {
 *     "name": "Maria Ouvinte",
 *     "cpf": "98765432100",
 *     "email": "maria@ouvinte.com",
 *     "password": "senha123",
 *     "favoriteGenres": ["pagode", "sertanejo"]   ← gêneros favoritos para o feed
 *   }
 *
 * Rota pública — não requer JWT.
 * Role é atribuída automaticamente como "lover".
 */
@RestController
@RequestMapping("/register")
public class ListenerController {

    private final ListenerService listenerService;

    public ListenerController(ListenerService listenerService) {
        this.listenerService = listenerService;
    }

    @PostMapping("/listener")
    public ResponseEntity<UserProfileResponse> registerListener(
            @RequestBody ListenerRegisterRequest request) {

        UserProfileResponse response = listenerService.registerListener(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
