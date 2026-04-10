package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.Event;
import xvgroup.icaros.domain.entity.EventProposal;
import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventProposalRepository extends JpaRepository<EventProposal, UUID> {

    // Propostas recebidas por um músico
    List<EventProposal> findByMusicianOrderByCreatedAtDesc(User musician);

    // Propostas enviadas por um produtor
    List<EventProposal> findByProducerOrderByCreatedAtDesc(User producer);

    // Propostas de um produtor para um evento específico
    List<EventProposal> findByEventAndProducer(Event event, User producer);

    // Verifica se já existe proposta pendente para o mesmo músico/evento
    boolean existsByEventAndMusicianAndStatus(Event event, User musician, EventProposal.Status status);

    // Propostas pendentes de um músico (badge de notificação)
    long countByMusicianAndStatus(User musician, EventProposal.Status status);
}
