package xvgroup.icaros.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import xvgroup.icaros.domain.entity.MusicalGenre;
import xvgroup.icaros.domain.entity.Role;
import xvgroup.icaros.domain.ports.MusicalGenreRepository;
import xvgroup.icaros.domain.ports.RoleRepository;

import java.util.Arrays;

@Configuration

public class DataLoader implements CommandLineRunner {


    private RoleRepository roleRepository;
    private MusicalGenreRepository musicalGenreRepository;
    

    public DataLoader( RoleRepository roleRepository,MusicalGenreRepository musicalGenreRepository) {
        this.roleRepository = roleRepository;
        this.musicalGenreRepository = musicalGenreRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(Role.Values.values())
                .map(Role.Values::toRole)
                .forEach(roleRepository::save);


        Arrays.stream( MusicalGenre.Values.values())
                .map(MusicalGenre.Values::toMusicalGenre)
                .forEach(musicalGenreRepository::save);







    }
}
