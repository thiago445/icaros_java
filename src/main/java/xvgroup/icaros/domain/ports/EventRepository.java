package xvgroup.icaros.domain.ports;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.Event;
import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    // Eventos criados por um produtor específico
    @Query("SELECT e FROM Event e WHERE e.eventLeader = :producer ORDER BY e.dateTime DESC")
    List<Event> findByProducer(@Param("producer") User producer);

    // Eventos em que um músico é performer confirmado
    @Query("SELECT e FROM Event e JOIN e.performers p WHERE p = :musician ORDER BY e.dateTime DESC")
    List<Event> findByPerformer(@Param("musician") User musician);

    // Eventos futuros paginados (feed de eventos)
    @Query(value = "SELECT e FROM Event e WHERE e.dateTime > CURRENT_TIMESTAMP ORDER BY e.dateTime ASC",
           countQuery = "SELECT COUNT(e) FROM Event e WHERE e.dateTime > CURRENT_TIMESTAMP")
    Page<Event> findUpcomingEvents(Pageable pageable);

    // Busca por gênero musical
    @Query("SELECT DISTINCT e FROM Event e JOIN e.musicalGenres g WHERE g.genre = :genre AND e.dateTime > CURRENT_TIMESTAMP ORDER BY e.dateTime ASC")
    List<Event> findByGenre(@Param("genre") String genre);
}
