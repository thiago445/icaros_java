package xvgroup.icaros.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.RequestProfile;
import xvgroup.icaros.application.dto.ResponseProfile;
import xvgroup.icaros.domain.entity.ProfileUser;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.ProfileUserRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileUserRepository profileUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProfileService(ProfileUserRepository profileUserRepository, UserRepository userRepository) {
        this.profileUserRepository = profileUserRepository;
        this.userRepository = userRepository;
    }

    public void createProfile(RequestProfile dto, String profilePictureUrl, String coverPhotoUrl,JwtAuthenticationToken token) {

            User user = userRepository.findById(UUID.fromString(token.getToken().getSubject()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        ProfileUser profile = new ProfileUser();
        profile.setNickName(dto.nickName());
        profile.setCity(dto.city());
        profile.setBio(dto.bio());
        profile.setUser(user);
        profile.setUrlProfile(profilePictureUrl);
        profile.setCoverPhotoUrl(coverPhotoUrl);

        profileUserRepository.save(profile);

    }

    public ResponseProfile getProfile(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        System.out.println("este é o usuario: "+ user);
        ProfileUser profile = profileUserRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        System.out.println("este é o profle: "+ profile);

        return ResponseProfile.toResponseProfile(profile);




    }

}
