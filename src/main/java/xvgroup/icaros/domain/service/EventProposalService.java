package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.ProposalAnswerRequest;
import xvgroup.icaros.application.dto.ProposalRequest;
import xvgroup.icaros.application.dto.ProposalResponse;
import xvgroup.icaros.domain.entity.*;
import xvgroup.icaros.domain.ports.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gerencia o ciclo de vida de propostas:
 *
 * Produtor → [POST /event/{id}/proposal] → Músico recebe proposta
 * Músico   → [PATCH /proposal/{id}/answer] → aceita ou recusa
 * Sistema  → adiciona músico como performer + notifica produtor
 */
@Service
public class EventProposalService {

    private final EventProposalRepository proposalRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public EventProposalService(EventProposalRepository proposalRepository,
                                EventRepository eventRepository,
                                UserRepository userRepository,
                                NotificationRepository notificationRepository) {
        this.proposalRepository = proposalRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    // ── PRODUTOR: Enviar proposta para músico ─────────────────────
    @Transactional
    public ProposalResponse sendProposal(UUID eventId, ProposalRequest request, JwtAuthenticationToken token) {
        UUID producerId = UUID.fromString(token.getToken().getSubject());

        User producer = userRepository.findById(producerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producer not found"));

        if (!isRole(producer, Role.Values.PRODUCER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only producers can send proposals");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getEventLeader().getUserId().equals(producerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only invite musicians to your own events");
        }

        User musician = userRepository.findById(request.musicianId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Musician not found"));

        if (!isRole(musician, Role.Values.MUSICIAN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target user is not a musician");
        }

        // Evita proposta duplicada pendente
        if (proposalRepository.existsByEventAndMusicianAndStatus(event, musician, EventProposal.Status.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "There is already a pending proposal for this musician in this event");
        }

        EventProposal proposal = new EventProposal();
        proposal.setEvent(event);
        proposal.setProducer(producer);
        proposal.setMusician(musician);
        proposal.setMessage(request.message());
        proposal.setStatus(EventProposal.Status.PENDING);

        proposalRepository.save(proposal);

        // Notifica o músico
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.MENTION);
        notification.setMessage(producer.getName() + " sent you a proposal for the event: " + event.getName());
        notification.setRecipient(musician);
        notification.setActor(producer);
        notificationRepository.save(notification);

        return ProposalResponse.fromEntity(proposal);
    }

    // ── MÚSICO: Responder proposta (aceitar ou recusar) ───────────
    @Transactional
    public ProposalResponse answerProposal(UUID proposalId, ProposalAnswerRequest answer, JwtAuthenticationToken token) {
        UUID musicianId = UUID.fromString(token.getToken().getSubject());

        EventProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));

        if (!proposal.getMusician().getUserId().equals(musicianId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This proposal is not addressed to you");
        }

        if (proposal.getStatus() != EventProposal.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This proposal has already been answered");
        }

        proposal.setResponseMessage(answer.responseMessage());
        proposal.setRespondedAt(Instant.now());

        if (answer.accepted()) {
            proposal.setStatus(EventProposal.Status.ACCEPTED);

            // Adiciona músico como performer no evento
            Event event = proposal.getEvent();
            if (event.getPerformers() == null) event.setPerformers(new HashSet<>());
            event.getPerformers().add(proposal.getMusician());
            eventRepository.save(event);

            // Notifica o produtor
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.MENTION);
            notification.setMessage(proposal.getMusician().getName() + " accepted your proposal for: " + event.getName());
            notification.setRecipient(proposal.getProducer());
            notification.setActor(proposal.getMusician());
            notificationRepository.save(notification);
        } else {
            proposal.setStatus(EventProposal.Status.DECLINED);

            // Notifica o produtor da recusa
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.MENTION);
            notification.setMessage(proposal.getMusician().getName() + " declined your proposal for: " + proposal.getEvent().getName());
            notification.setRecipient(proposal.getProducer());
            notification.setActor(proposal.getMusician());
            notificationRepository.save(notification);
        }

        proposalRepository.save(proposal);
        return ProposalResponse.fromEntity(proposal);
    }

    // ── PRODUTOR: Cancelar proposta ───────────────────────────────
    @Transactional
    public ProposalResponse cancelProposal(UUID proposalId, JwtAuthenticationToken token) {
        UUID producerId = UUID.fromString(token.getToken().getSubject());

        EventProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));

        if (!proposal.getProducer().getUserId().equals(producerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own proposals");
        }

        if (proposal.getStatus() != EventProposal.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel a proposal that is not pending");
        }

        proposal.setStatus(EventProposal.Status.CANCELLED);
        proposalRepository.save(proposal);
        return ProposalResponse.fromEntity(proposal);
    }

    // ── MÚSICO: Minhas propostas recebidas ────────────────────────
    public List<ProposalResponse> getMyProposalsAsMusician(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());
        User musician = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return proposalRepository.findByMusicianOrderByCreatedAtDesc(musician).stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── PRODUTOR: Propostas que enviei ───────────────────────────
    public List<ProposalResponse> getMyProposalsAsProducer(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());
        User producer = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return proposalRepository.findByProducerOrderByCreatedAtDesc(producer).stream()
                .map(ProposalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── MÚSICO: Badge — quantas propostas pendentes ───────────────
    public long countPendingProposals(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());
        User musician = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return proposalRepository.countByMusicianAndStatus(musician, EventProposal.Status.PENDING);
    }

    // ── HELPER ───────────────────────────────────────────────────
    private boolean isRole(User user, Role.Values roleValue) {
        return user.getRole() != null &&
               roleValue.getDescription().equalsIgnoreCase(user.getRole().getDescription());
    }
}
