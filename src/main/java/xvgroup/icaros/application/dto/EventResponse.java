package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.Event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record EventResponse(
        UUID id,
        String name,
        String description,
        String location,
        Instant dateTime,
        Instant createdAt,
        UserResponse producer,
        List<String> musicalGenres,
        List<UserResponse> performers,
        int attendeesCount
) {
    public static EventResponse fromEntity(Event e) {
        List<String> genres = e.getMusicalGenres() != null
                ? e.getMusicalGenres().stream().map(g -> g.getGenre()).collect(Collectors.toList())
                : List.of();

        List<UserResponse> performers = e.getPerformers() != null
                ? e.getPerformers().stream().map(UserResponse::fromEntity).collect(Collectors.toList())
                : List.of();

        int attendees = e.getAttendees() != null ? e.getAttendees().size() : 0;

        return new EventResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getLocation(),
                e.getDateTime(),
                e.getCreationTimesTamp(),
                UserResponse.fromEntity(e.getEventLeader()),
                genres,
                performers,
                attendees
        );
    }
}
