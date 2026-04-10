package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.UserProfileResponse;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MusicianDiscoveryService {

    private final UserRepository userRepository;

    public MusicianDiscoveryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserProfileResponse> getAllMusicians() {
        return userRepository.findAllMusicians().stream()
                .map(UserProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserProfileResponse> getMusiciansByGenre(String genre) {
        return userRepository.findMusiciansByGenre(genre).stream()
                .map(UserProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserProfileResponse getMusicianProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Musician not found"));

        if (user.getRole() == null || !"musician".equalsIgnoreCase(user.getRole().getDescription())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not a musician");
        }

        return UserProfileResponse.fromEntity(user);
    }
}
