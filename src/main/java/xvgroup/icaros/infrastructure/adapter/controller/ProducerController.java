package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xvgroup.icaros.application.dto.ProducerRegisterRequest;
import xvgroup.icaros.application.dto.UserProfileResponse;
import xvgroup.icaros.domain.service.ProducerService;

/**
 * ProducerController — cadastro de produtores musicais.
 *
 * POST /register/producer
 *   Body:
 *   {
 *     "name": "Carlos Produtor",
 *     "cpf": "12345678900",
 *     "email": "carlos@produtor.com",
 *     "password": "senha123",
 *     "genrePreferences": ["samba", "funk"]   ← gêneros que quer ver no feed
 *   }
 *
 * Rota pública — não requer JWT (usuário ainda não tem conta).
 * Role é atribuída automaticamente como "producer".
 */
@RestController
@RequestMapping("/register")
public class ProducerController {

    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/producer")
    public ResponseEntity<UserProfileResponse> registerProducer(
            @RequestBody ProducerRegisterRequest request) {

        UserProfileResponse response = producerService.registerProducer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
