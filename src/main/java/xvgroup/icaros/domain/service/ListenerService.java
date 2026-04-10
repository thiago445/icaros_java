package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.ListenerRegisterRequest;
import xvgroup.icaros.application.dto.UserProfileResponse;
import xvgroup.icaros.domain.entity.MusicalGenre;
import xvgroup.icaros.domain.entity.Role;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.MusicalGenreRepository;
import xvgroup.icaros.domain.ports.RoleRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.Set;

/**
 * Service de cadastro exclusivo para ouvintes (lovers).
 *
 * Diferenças em relação ao cadastro genérico:
 *  - Role fixada em "lover" automaticamente
 *  - favoriteGenres alimentam o algoritmo de scoring do feed (mesma lógica do produtor)
 *  - Ouvintes não produzem conteúdo, apenas consomem e interagem
 */
@Service
public class ListenerService {

    private final UserRepository userRepository;
    private final MusicalGenreRepository musicalGenreRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ListenerService(UserRepository userRepository,
                           MusicalGenreRepository musicalGenreRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.musicalGenreRepository = musicalGenreRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfileResponse registerListener(ListenerRegisterRequest request) {

        // Validação de unicidade
        if (userRepository.findByCpf(request.cpf()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "CPF already registered in the platform");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already registered in the platform");
        }

        // Role sempre "lover"
        Role loverRole = roleRepository.findByDescription(Role.Values.LOVER.getDescription());
        if (loverRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Role 'lover' not configured in database");
        }

        // Gêneros favoritos — usados no scoring do feed
        Set<MusicalGenre> favoriteGenres = Set.of();
        if (request.favoriteGenres() != null && !request.favoriteGenres().isEmpty()) {
            favoriteGenres = musicalGenreRepository.findByGenreIn(request.favoriteGenres());
        }

        User listener = new User();
        listener.setName(request.name());
        listener.setCpf(request.cpf());
        listener.setEmail(request.email());
        listener.setPassword(passwordEncoder.encode(request.password()));
        listener.setRole(loverRole);
        listener.setMusicalGender(favoriteGenres);

        userRepository.save(listener);

        return UserProfileResponse.fromEntity(listener);
    }
}
