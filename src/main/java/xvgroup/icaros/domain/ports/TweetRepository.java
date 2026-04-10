package xvgroup.icaros.domain.ports;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.Tweet;
import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    // Busca post único com likes carregados (para like/detalhe)
    @Query("SELECT t FROM Tweet t LEFT JOIN FETCH t.likes WHERE t.id = :id")
    Optional<Tweet> findByIdWithLikes(@Param("id") Long id);

    // Posts de um usuário específico — paginado
    @Query(value = "SELECT DISTINCT t FROM Tweet t LEFT JOIN FETCH t.likes LEFT JOIN FETCH t.comments " +
                   "WHERE t.user = :user ORDER BY t.creationTimestamp DESC",
           countQuery = "SELECT COUNT(t) FROM Tweet t WHERE t.user = :user")
    Page<Tweet> findByUser(@Param("user") User user, Pageable pageable);

    // Feed geral paginado — todos os posts independente de role
    @Query(value = "SELECT DISTINCT t FROM Tweet t LEFT JOIN FETCH t.likes " +
                   "ORDER BY t.creationTimestamp DESC",
           countQuery = "SELECT COUNT(t) FROM Tweet t")
    Page<Tweet> findAllPaged(Pageable pageable);

    // Feed filtrado: somente posts de MÚSICOS — base para o feed personalizado do produtor
    @Query(value = "SELECT DISTINCT t FROM Tweet t " +
                   "LEFT JOIN FETCH t.likes " +
                   "JOIN t.user u " +
                   "JOIN u.role r " +
                   "WHERE r.description = 'musician' " +
                   "ORDER BY t.creationTimestamp DESC",
           countQuery = "SELECT COUNT(t) FROM Tweet t JOIN t.user u JOIN u.role r WHERE r.description = 'musician'")
    Page<Tweet> findAllByMusicians(Pageable pageable);

    // Compatível com código legado (getAllTweets sem paginação)
    @Query("SELECT DISTINCT t FROM Tweet t LEFT JOIN FETCH t.likes ORDER BY t.creationTimestamp DESC")
    List<Tweet> findAllOrderByCreationTimestampDesc();

    // Versão legada por usuário (sem paginação)
    @Query("SELECT DISTINCT t FROM Tweet t LEFT JOIN FETCH t.likes LEFT JOIN FETCH t.comments " +
           "WHERE t.user = :user ORDER BY t.creationTimestamp DESC")
    List<Tweet> findByUserOrderByCreationTimestampDesc(@Param("user") User user);
}
