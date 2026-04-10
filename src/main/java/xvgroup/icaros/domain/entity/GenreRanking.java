package xvgroup.icaros.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Snapshot do ranking de gêneros musicais na plataforma.
 * Calculado com base em: curtidas + comentários + seguidores dos músicos daquele gênero.
 * Atualizado sob demanda (endpoint de refresh) ou automaticamente via score.
 */
@Entity
@Table(name = "tb_genre_ranking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenreRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "musicalGenre_Id")
    private MusicalGenre genre;


    @Column(nullable = false)
    private long totalLikes = 0;

    @Column(nullable = false)
    private long totalComments = 0;

    @Column(nullable = false)
    private long totalPosts = 0;

    @Column(nullable = false)
    private long totalMusicians = 0;

    @Column(nullable = false)
    private double rankingScore = 0.0;

    @UpdateTimestamp
    private Instant lastUpdated;
}
