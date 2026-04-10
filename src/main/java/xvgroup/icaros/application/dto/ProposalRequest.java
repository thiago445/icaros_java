package xvgroup.icaros.application.dto;

import java.util.UUID;

public record ProposalRequest(
        UUID musicianId,
        String message   // mensagem personalizada para o músico
) {}
