package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String name,
        String nickname,
        String urlImage
) {
    public static UserResponse fromEntity(User user) {
        String urlprofile = null;
        String nickname = null;

        if (user.getProfileUser() != null) {
            String raw = user.getProfileUser().getUrlProfile();
            if (raw != null && !raw.isEmpty()) {
                urlprofile = raw;
            }
            nickname = user.getProfileUser().getNickName();
        }

        return new UserResponse(
                user.getUserId(),
                user.getName(),
                nickname,
                urlprofile
        );
    }
}
