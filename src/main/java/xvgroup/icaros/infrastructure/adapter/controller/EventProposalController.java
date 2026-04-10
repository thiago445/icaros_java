package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.ProposalAnswerRequest;
import xvgroup.icaros.application.dto.ProposalRequest;
import xvgroup.icaros.application.dto.ProposalResponse;
import xvgroup.icaros.domain.service.EventProposalService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EventProposalController — gerencia propostas de produtores para músicos.
 *
 * POST   /events/{eventId}/proposals          → Produtor envia proposta ao músico
 * GET    /proposals/received                  → Músico vê propostas recebidas
 * GET    /proposals/sent                      → Produtor vê propostas enviadas
 * GET    /proposals/pending/count             → Badge de pendentes (músico)
 * PATCH  /proposals/{id}/answer               → Músico aceita ou recusa
 * DELETE /proposals/{id}                      → Produtor cancela proposta pendente
 */
@RestController
public class EventProposalController {

    private final EventProposalService proposalService;

    public EventProposalController(EventProposalService proposalService) {
        this.proposalService = proposalService;
    }

    // PRODUTOR: Enviar proposta para músico
    @PostMapping("/events/{eventId}/proposals")
    public ResponseEntity<ProposalResponse> sendProposal(@PathVariable UUID eventId,
                                                          @RequestBody ProposalRequest request,
                                                          JwtAuthenticationToken token) {
        ProposalResponse response = proposalService.sendProposal(eventId, request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // MÚSICO: Propostas recebidas
    @GetMapping("/proposals/received")
    public ResponseEntity<List<ProposalResponse>> getReceivedProposals(JwtAuthenticationToken token) {
        List<ProposalResponse> proposals = proposalService.getMyProposalsAsMusician(token);
        return ResponseEntity.ok(proposals);
    }

    // PRODUTOR: Propostas enviadas
    @GetMapping("/proposals/sent")
    public ResponseEntity<List<ProposalResponse>> getSentProposals(JwtAuthenticationToken token) {
        List<ProposalResponse> proposals = proposalService.getMyProposalsAsProducer(token);
        return ResponseEntity.ok(proposals);
    }

    // MÚSICO: Quantidade de propostas pendentes (badge no menu)
    @GetMapping("/proposals/pending/count")
    public ResponseEntity<Map<String, Long>> countPendingProposals(JwtAuthenticationToken token) {
        long count = proposalService.countPendingProposals(token);
        return ResponseEntity.ok(Map.of("pending", count));
    }

    // MÚSICO: Responder proposta (aceitar ou recusar)
    @PatchMapping("/proposals/{id}/answer")
    public ResponseEntity<ProposalResponse> answerProposal(@PathVariable UUID id,
                                                            @RequestBody ProposalAnswerRequest answer,
                                                            JwtAuthenticationToken token) {
        ProposalResponse response = proposalService.answerProposal(id, answer, token);
        return ResponseEntity.ok(response);
    }

    // PRODUTOR: Cancelar proposta pendente
    @DeleteMapping("/proposals/{id}")
    public ResponseEntity<ProposalResponse> cancelProposal(@PathVariable UUID id,
                                                            JwtAuthenticationToken token) {
        ProposalResponse response = proposalService.cancelProposal(id, token);
        return ResponseEntity.ok(response);
    }
}
