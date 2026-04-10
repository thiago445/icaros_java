package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.EventRequest;
import xvgroup.icaros.application.dto.EventResponse;
import xvgroup.icaros.domain.service.EventService;

import java.util.List;
import java.util.UUID;

/**
 * EventController — gerencia o ciclo de vida dos eventos na plataforma Icaros.
 *
 * Regras de negócio:
 *   - Apenas PRODUTORES podem criar, editar e deletar eventos
 *   - Qualquer usuário autenticado pode ver eventos e confirmar presença (RSVP)
 *   - GET /events e GET /events/{id} são públicos (sem JWT)
 */
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // POST /events — criar evento (somente PRODUTOR)
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request,
                                                     JwtAuthenticationToken token) {
        EventResponse response = eventService.createEvent(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /events/{id} — atualizar evento (somente dono)
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID id,
                                                     @RequestBody EventRequest request,
                                                     JwtAuthenticationToken token) {
        EventResponse response = eventService.updateEvent(id, request, token);
        return ResponseEntity.ok(response);
    }

    // DELETE /events/{id} — deletar evento (somente dono)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id,
                                            JwtAuthenticationToken token) {
        eventService.deleteEvent(id, token);
        return ResponseEntity.noContent().build();
    }

    // GET /events — listar eventos futuros paginado (público)
    @GetMapping
    public ResponseEntity<List<EventResponse>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EventResponse> events = eventService.getUpcomingEvents(page, size);
        return ResponseEntity.ok(events);
    }

    // GET /events/{id} — detalhe de evento (público)
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    // GET /events/genre/{genre} — eventos por gênero (público)
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<EventResponse>> getEventsByGenre(@PathVariable String genre) {
        List<EventResponse> events = eventService.getEventsByGenre(genre);
        return ResponseEntity.ok(events);
    }

    // GET /events/me — meus eventos (produtor vê os que criou, músico vê os que participa)
    @GetMapping("/me")
    public ResponseEntity<List<EventResponse>> getMyEvents(JwtAuthenticationToken token) {
        List<EventResponse> events = eventService.getMyEvents(token);
        return ResponseEntity.ok(events);
    }

    // POST /events/{id}/rsvp — confirmar/cancelar presença (toggle)
    @PostMapping("/{id}/rsvp")
    public ResponseEntity<EventResponse> rsvp(@PathVariable UUID id,
                                               JwtAuthenticationToken token) {
        EventResponse response = eventService.rsvpEvent(id, token);
        return ResponseEntity.ok(response);
    }
}
