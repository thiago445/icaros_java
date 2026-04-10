package xvgroup.icaros.domain.service;


import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import xvgroup.icaros.application.dto.LoginRequest;
import xvgroup.icaros.application.dto.LoginResponse;
import xvgroup.icaros.domain.entity.Role;
import xvgroup.icaros.domain.ports.UserRepository;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final JwtEncoder jwtEncoder;

    public TokenService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponse Login(LoginRequest loginRequest){
        var user = userRepository.findByEmail(loginRequest.email());

        if(user.isEmpty()||!user.get().isLoginCorrect(loginRequest, bCryptPasswordEncoder)){
            throw new BadCredentialsException("user or password is invalid!");
        }
        var now= Instant.now();
        var expiresIn = 300l;

        var scopes = user.get().getRole();

        var claims = JwtClaimsSet.builder()
                .issuer("icarosBackend")
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();
        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginResponse(jwtValue, expiresIn);
    }
}
