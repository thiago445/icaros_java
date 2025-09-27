package xvgroup.icaros.domain.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import xvgroup.icaros.application.dto.UserRequestDto;
import xvgroup.icaros.domain.entity.MusicalGenre;
import xvgroup.icaros.domain.entity.Role;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.MusicalGenreRepository;
import xvgroup.icaros.domain.ports.RoleRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.*;

@Service
public class UserService {


    UserRepository userReposiory;
    MusicalGenreRepository musicalGenreRepository;
    RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userReposiory, MusicalGenreRepository musicalGenreRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userReposiory = userReposiory;
        this.musicalGenreRepository = musicalGenreRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void createUser(UserRequestDto dto){

        Set<MusicalGenre> musicalGenre = musicalGenreRepository.findByGenreIn(dto.musicalGenre());

        Role userRole = roleRepository.findByDescription(dto.role());

        User user= new User();
        user.setRole(userRole);
        user.setName(dto.name());
        user.setCpf(dto.cpf());
        user.setEmail(dto.email());
        user.setMusicalGender(musicalGenre);
        user.setPassword(bCryptPasswordEncoder.encode(dto.password()));
        userReposiory.save(user);

    }
    public Optional<User> findByCpf(String cpf){
        return userReposiory.findByCpf(cpf);
        
    }

}
