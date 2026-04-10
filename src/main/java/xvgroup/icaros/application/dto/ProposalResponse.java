package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.EventProposal;

import java.time.Instant;
import java.util.UUID;

public record ProposalResponse(
        UUID id,
        EventResponse event,
        UserResponse producer,
        UserResponse musician,
        String message,
        String responseMessage,
        String status,
        Instant createdAt,
        Instant respondedAt
) {
    public static ProposalResponse fromEntity(EventProposal p) {
        return new ProposalResponse(
                p.getId(),
                EventResponse.fromEntity(p.getEvent()),
                UserResponse.fromEntity(p.getProducer()),
                UserResponse.fromEntity(p.getMusician()),
                p.getMessage(),
                p.getResponseMessage(),
                p.getStatus().name(),
                p.getCreatedAt(),
                p.getRespondedAt()
        );
    }
}
