package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.MusicalGenre;

import java.util.List;
import java.util.Set;

@Repository
public interface MusicalGenreRepository extends JpaRepository<MusicalGenre, Long> {

    Set<MusicalGenre> findByGenreIn(List<String> strings);
}
