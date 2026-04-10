package xvgroup.icaros.application.dto;

public record ProposalAnswerRequest(
        boolean accepted,
        String responseMessage  // mensagem opcional do músico para o produtor
) {}
