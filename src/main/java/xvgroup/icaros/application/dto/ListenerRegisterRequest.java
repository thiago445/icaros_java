package xvgroup.icaros.application.dto;

import java.util.List;

/**
 * DTO para cadastro de ouvinte (listener/lover).
 * Também informa gêneros favoritos para personalização do feed.
 */
public record ListenerRegisterRequest(
        String name,
        String cpf,
        String email,
        String password,
        List<String> favoriteGenres     // gêneros favoritos que personalizam o feed
) {
}
