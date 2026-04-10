package xvgroup.icaros.application.dto;

import java.util.List;

/**
 * DTO para cadastro de produtor musical.
 * Inclui preferências de gênero que alimentam o algoritmo de scoring do feed.
 */
public record ProducerRegisterRequest(
        String name,
        String cpf,
        String email,
        String password,
        List<String> genrePreferences   // gêneros que o produtor quer ver no feed
) {
}
