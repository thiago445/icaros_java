package xvgroup.icaros.application.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO para criação de evento pelo PRODUTOR.
 * O produtor é identificado pelo JWT — não precisa informar no body.
 */
public record EventRequest(
        String name,
        String description,
        String location,
        Instant dateTime,
        List<String> musicalGenres   // gêneros do evento
) {}
