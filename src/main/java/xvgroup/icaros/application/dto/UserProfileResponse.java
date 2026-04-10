package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resposta de perfil completo, incluindo role e gêneros musicais.
 * Usado nos endpoints de cadastro de produtor, músico e ouvinte.
 */
public record UserProfileResponse(
        UUID userId,
        String name,
        String email,
        String role,
        List<String> musicalGenres,
        String nickname,
        String urlProfile,
        String city,
        String bio
) {
    public static UserProfileResponse fromEntity(User user) {
        List<String> genres = user.getMusicalGender() != null
                ? user.getMusicalGender().stream()
                        .map(g -> g.getGenre())
                        .collect(Collectors.toList())
                : List.of();

        String nickname = null;
        String urlProfile = null;
        String city = null;
        String bio = null;

        if (user.getProfileUser() != null) {
            nickname = user.getProfileUser().getNickName();
            urlProfile = user.getProfileUser().getUrlProfile();
            city = user.getProfileUser().getCity();
            bio = user.getProfileUser().getBio();
        }

        return new UserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getDescription() : null,
                genres,
                nickname,
                urlProfile,
                city,
                bio
        );
    }
}
