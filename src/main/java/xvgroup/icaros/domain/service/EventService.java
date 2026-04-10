package xvgroup.icaros.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.EventRequest;
import xvgroup.icaros.application.dto.EventResponse;
import xvgroup.icaros.domain.entity.Event;
import xvgroup.icaros.domain.entity.MusicalGenre;
import xvgroup.icaros.domain.entity.Role;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.EventRepository;
import xvgroup.icaros.domain.ports.MusicalGenreRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final MusicalGenreRepository musicalGenreRepository;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        MusicalGenreRepository musicalGenreRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.musicalGenreRepository = musicalGenreRepository;
    }

    // ── PRODUTOR: Criar evento ────────────────────────────────────
    @Transactional
    public EventResponse createEvent(EventRequest request, JwtAuthenticationToken token) {
        UUID producerId = UUID.fromString(token.getToken().getSubject());

        User producer = userRepository.findById(producerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!isProducer(producer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only producers can create events");
        }

        Set<MusicalGenre> genres = new HashSet<>();
        if (request.musicalGenres() != null && !request.musicalGenres().isEmpty()) {
            genres = musicalGenreRepository.findByGenreIn(request.musicalGenres());
        }

        Event event = new Event();
        event.setName(request.name());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setDateTime(request.dateTime());
        event.setEventLeader(producer);
        event.setMusicalGenres(genres);
        event.setPerformers(new HashSet<>());
        event.setAttendees(new HashSet<>());

        eventRepository.save(event);
        return EventResponse.fromEntity(event);
    }

    // ── PRODUTOR: Atualizar evento ────────────────────────────────
    @Transactional
    public EventResponse updateEvent(UUID eventId, EventRequest request, JwtAuthenticationToken token) {
        UUID producerId = UUID.fromString(token.getToken().getSubject());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getEventLeader().getUserId().equals(producerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the event creator can update it");
        }

        Set<MusicalGenre> genres = new HashSet<>();
        if (request.musicalGenres() != null && !request.musicalGenres().isEmpty()) {
            genres = musicalGenreRepository.findByGenreIn(request.musicalGenres());
        }

        event.setName(request.name());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setDateTime(request.dateTime());
        event.setMusicalGenres(genres);

        eventRepository.save(event);
        return EventResponse.fromEntity(event);
    }

    // ── PRODUTOR: Deletar evento ──────────────────────────────────
    @Transactional
    public void deleteEvent(UUID eventId, JwtAuthenticationToken token) {
        UUID producerId = UUID.fromString(token.getToken().getSubject());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getEventLeader().getUserId().equals(producerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the event creator can delete it");
        }

        eventRepository.delete(event);
    }

    // ── PÚBLICO: Listar eventos futuros ───────────────────────────
    public List<EventResponse> getUpcomingEvents(int page, int size) {
        Page<Event> events = eventRepository.findUpcomingEvents(PageRequest.of(page, size));
        return events.getContent().stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── PÚBLICO: Buscar eventos por gênero ────────────────────────
    public List<EventResponse> getEventsByGenre(String genre) {
        return eventRepository.findByGenre(genre).stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── PÚBLICO: Detalhe de evento ────────────────────────────────
    public EventResponse getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return EventResponse.fromEntity(event);
    }

    // ── AUTENTICADO: Meus eventos (produtor ou músico) ────────────
    public List<EventResponse> getMyEvents(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Event> events;
        if (isProducer(user)) {
            events = eventRepository.findByProducer(user);
        } else {
            events = eventRepository.findByPerformer(user);
        }

        return events.stream().map(EventResponse::fromEntity).collect(Collectors.toList());
    }

    // ── OUVINTE/QUALQUER: RSVP — confirmar presença ───────────────
    @Transactional
    public EventResponse rsvpEvent(UUID eventId, JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        Set<User> attendees = event.getAttendees();
        if (attendees == null) attendees = new HashSet<>();

        if (attendees.stream().anyMatch(a -> a.getUserId().equals(userId))) {
            attendees.removeIf(a -> a.getUserId().equals(userId)); // toggle out
        } else {
            attendees.add(user); // toggle in
        }

        event.setAttendees(attendees);
        eventRepository.save(event);
        return EventResponse.fromEntity(event);
    }

    // ── HELPER ───────────────────────────────────────────────────
    private boolean isProducer(User user) {
        return user.getRole() != null &&
               Role.Values.PRODUCER.getDescription().equalsIgnoreCase(user.getRole().getDescription());
    }
}
