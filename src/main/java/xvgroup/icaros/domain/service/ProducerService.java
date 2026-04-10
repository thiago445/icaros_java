package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.ProducerRegisterRequest;
import xvgroup.icaros.application.dto.UserProfileResponse;
import xvgroup.icaros.domain.entity.MusicalGenre;
import xvgroup.icaros.domain.entity.Role;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.MusicalGenreRepository;
import xvgroup.icaros.domain.ports.RoleRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.Set;

/**
 * Service de cadastro exclusivo para produtores musicais.
 *
 * Diferenças em relação ao cadastro genérico:
 *  - Role fixada em "producer" automaticamente (sem precisar informar no body)
 *  - genrePreferences são os gêneros que o produtor quer descobrir (alimentam o scoring do feed)
 *  - Validação de CPF e e-mail únicos com mensagens específicas
 */
@Service
public class ProducerService {

    private final UserRepository userRepository;
    private final MusicalGenreRepository musicalGenreRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ProducerService(UserRepository userRepository,
                           MusicalGenreRepository musicalGenreRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.musicalGenreRepository = musicalGenreRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfileResponse registerProducer(ProducerRegisterRequest request) {

        // Validação de unicidade
        if (userRepository.findByCpf(request.cpf()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "CPF already registered in the platform");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already registered in the platform");
        }

        // Role sempre "producer" — não depende do body
        Role producerRole = roleRepository.findByDescription(Role.Values.PRODUCER.getDescription());
        if (producerRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Role 'producer' not configured in database");
        }

        // Resolve gêneros de preferência (os que ele quer ver no feed)
        Set<MusicalGenre> preferredGenres = Set.of();
        if (request.genrePreferences() != null && !request.genrePreferences().isEmpty()) {
            preferredGenres = musicalGenreRepository.findByGenreIn(request.genrePreferences());
        }

        User producer = new User();
        producer.setName(request.name());
        producer.setCpf(request.cpf());
        producer.setEmail(request.email());
        producer.setPassword(passwordEncoder.encode(request.password()));
        producer.setRole(producerRole);
        producer.setMusicalGender(preferredGenres);

        userRepository.save(producer);

        return UserProfileResponse.fromEntity(producer);
    }
}
