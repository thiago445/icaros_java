package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.ProfileUser;

public record ResponseProfile(String urlProfile,
                              String nickName,
                              String coverPhotoUrl,
                              String bio,
                              String city) {
    public static ResponseProfile toResponseProfile(ProfileUser profileUser){
        return  new ResponseProfile(profileUser.getUrlProfile(),
                profileUser.getNickName(),
                profileUser.getCoverPhotoUrl(),
                profileUser.getBio(),
                profileUser.getCity());

    }
}
