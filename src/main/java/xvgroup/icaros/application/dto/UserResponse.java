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
        String urlprofile= null;
        if(!user.getProfileUser().getUrlProfile().isEmpty()){
            urlprofile= user.getProfileUser().getUrlProfile();
        }

        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getProfileUser().getNickName(),
                urlprofile
        );
    }
}
