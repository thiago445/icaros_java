package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.GenreRanking;
import xvgroup.icaros.domain.entity.MusicalGenre;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRankingRepository extends JpaRepository<GenreRanking, MusicalGenre> {

    // Ranking ordenado por score decrescente
    @Query("SELECT g FROM GenreRanking g ORDER BY g.rankingScore DESC")
    List<GenreRanking> findAllOrderByScore();

    Optional<GenreRanking> findByGenre(MusicalGenre genre);
}
