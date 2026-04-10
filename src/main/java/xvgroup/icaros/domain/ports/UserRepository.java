package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCpf(String cpf);

    Optional<User> findByEmail(String email);

    // Busca todos os usuários com role "musician" — usado para propostas e ranking
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.description = 'musician'")
    List<User> findAllMusicians();

    // Músicos por gênero — produtor usa para descobrir talentos
    @Query("SELECT DISTINCT u FROM User u JOIN u.role r JOIN u.musicalGender g WHERE r.description = 'musician' AND g.genre = :genre")
    List<User> findMusiciansByGenre(@Param("genre") String genre);

    // Conta posts de um músico — usado no ranking de gênero
    @Query("SELECT COUNT(t) FROM Tweet t JOIN t.user u WHERE u = :user")
    long countPostsByUser(@Param("user") User user);
}
