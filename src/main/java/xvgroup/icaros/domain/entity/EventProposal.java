package xvgroup.icaros.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa uma proposta enviada por um PRODUTOR para um MÚSICO
 * participar de um evento como performer.
 *
 * Fluxo:
 *   Produtor cria evento → envia proposta ao músico →
 *   Músico aceita ou recusa → notificação automática ao produtor
 */
@Entity
@Table(name = "tb_event_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventProposal {

    public enum Status {
        PENDING,    // aguardando resposta do músico
        ACCEPTED,   // músico aceitou
        DECLINED,   // músico recusou
        CANCELLED   // produtor cancelou a proposta
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_id", nullable = false)
    private User producer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musician_id", nullable = false)
    private User musician;

    @Column(columnDefinition = "TEXT")
    private String message;  // mensagem personalizada do produtor

    @Column(columnDefinition = "TEXT")
    private String responseMessage; // resposta do músico ao aceitar/recusar

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @CreationTimestamp
    private Instant createdAt;

    private Instant respondedAt;
}
