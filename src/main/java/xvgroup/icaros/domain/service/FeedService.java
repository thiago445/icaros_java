package xvgroup.icaros.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.FeedPage;
import xvgroup.icaros.application.dto.ScoredTweet;
import xvgroup.icaros.application.dto.TweetDetailResponse;
import xvgroup.icaros.domain.entity.MusicalGenre;
import xvgroup.icaros.domain.entity.Role;
import xvgroup.icaros.domain.entity.Tweet;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.TweetRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * FeedService — responsável pelo feed inteligente e personalizado da rede social Icaros.
 *
 * ALGORITMO DE SCORING:
 * ─────────────────────────────────────────────────────────────────
 *  score = (BASE_SCORE + genreBonus + engagementBonus) / timeDecay
 *
 *  BASE_SCORE     = 10.0  (todo post começa com 10 pontos)
 *  genreBonus     = +15.0 se o gênero do músico bate com a preferência do usuário logado
 *  engagementBonus= +0.5 por curtida + +1.0 por comentário (engajamento da comunidade)
 *  timeDecay      = fator calculado com ChronoUnit.HOURS
 *                   Posts < 6h:   decay = 1.0   (sem penalidade)
 *                   Posts 6-24h:  decay = 1.5
 *                   Posts 24-48h: decay = 3.0
 *                   Posts > 48h:  decay = 8.0   (peso drástico conforme especificado)
 * ─────────────────────────────────────────────────────────────────
 *
 * FILTRAGEM POR ROLE:
 *   - Feed do PRODUTOR: vê apenas posts de MÚSICOS, score com genreBonus baseado em suas preferências
 *   - Feed do MÚSICO:   vê posts de todos os músicos + próprios posts
 *   - Feed do OUVINTE:  vê posts de músicos, score com genreBonus baseado nos gêneros favoritos
 *   - Feed genérico:    fallback sem filtro de role
 */
@Service
public class FeedService {

    // ── Constantes do algoritmo ──────────────────────────────────
    private static final double BASE_SCORE          = 10.0;
    private static final double GENRE_MATCH_BONUS   = 15.0;
    private static final double LIKE_BONUS          = 0.5;
    private static final double COMMENT_BONUS       = 1.0;

    // Time-decay thresholds (em horas)
    private static final long   DECAY_THRESHOLD_FRESH      = 6L;
    private static final long   DECAY_THRESHOLD_RECENT     = 24L;
    private static final long   DECAY_THRESHOLD_OLD        = 48L;

    private static final double DECAY_FRESH    = 1.0;   // < 6h: sem penalidade
    private static final double DECAY_RECENT   = 1.5;   // 6-24h: leve
    private static final double DECAY_OLD      = 3.0;   // 24-48h: moderado
    private static final double DECAY_EXPIRED  = 8.0;   // > 48h: drástico

    // ── Dependências ─────────────────────────────────────────────
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public FeedService(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // FEED PERSONALIZADO — paginado + scored + filtrado por role
    // ─────────────────────────────────────────────────────────────

    /**
     * Retorna o feed personalizado com scoring para o usuário autenticado.
     * A filtragem por tipo de usuário (músico/produtor/ouvinte) é feita automaticamente.
     *
     * @param page  Número da página (começa em 0)
     * @param size  Quantidade de posts por página (recomendado: 20)
     */
    public FeedPage getPersonalizedFeed(int page, int size, JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getToken().getSubject());

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String userRole = currentUser.getRole() != null
                ? currentUser.getRole().getDescription()
                : "unknown";

        // Busca a página de posts crus do banco (já filtrado por músicos se necessário)
        Pageable pageable = PageRequest.of(page, size);
        Page<Tweet> tweetPage = fetchTweetsByRole(userRole, pageable);

        // Extrai os gêneros do usuário logado para o cálculo de bônus
        Set<String> userGenres = extractGenreNames(currentUser.getMusicalGender());

        // Pipeline de scoring com Java Streams:
        // 1. Para cada tweet, calcula o score
        // 2. Ordena por score decrescente
        // 3. Converte para o DTO de resposta
        List<TweetDetailResponse> scoredAndSorted = tweetPage.getContent().stream()
                .map(tweet -> new ScoredTweet(tweet, calculateScore(tweet, userGenres)))
                .sorted(Comparator.comparing(ScoredTweet::score).reversed())
                .map(st -> TweetDetailResponse.fromEntity(st.tweet(), currentUserId))
                .collect(Collectors.toList());

        return new FeedPage(
                scoredAndSorted,
                tweetPage.getNumber(),
                tweetPage.getSize(),
                tweetPage.getTotalElements(),
                tweetPage.getTotalPages(),
                tweetPage.isLast()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // FEED PÚBLICO — sem autenticação, sem scoring
    // ─────────────────────────────────────────────────────────────

    /**
     * Feed público sem personalização — usado para visitantes não autenticados.
     * Ordenação simples por data, sem scoring.
     */
    public FeedPage getPublicFeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tweet> tweetPage = tweetRepository.findAllPaged(pageable);

        List<TweetDetailResponse> content = tweetPage.getContent().stream()
                .map(tweet -> TweetDetailResponse.fromEntity(tweet, null))
                .collect(Collectors.toList());

        return new FeedPage(
                content,
                tweetPage.getNumber(),
                tweetPage.getSize(),
                tweetPage.getTotalElements(),
                tweetPage.getTotalPages(),
                tweetPage.isLast()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // ALGORITMO DE SCORING
    // ─────────────────────────────────────────────────────────────

    /**
     * Calcula o score de relevância de um tweet para um usuário específico.
     *
     * Fórmula:
     *   score = (BASE_SCORE + genreBonus + engagementBonus) / timeDecay
     *
     * Exemplo prático:
     *   Post de músico de samba, com 5 likes e 2 comentários, publicado há 2h,
     *   sendo visto por produtor que prefere samba:
     *   score = (10 + 15 + 5*0.5 + 2*1.0) / 1.0 = 29.5
     *
     *   Mesmo post após 60h:
     *   score = (10 + 15 + 2.5 + 2.0) / 8.0 = 3.69  ← drástica queda
     */
    double calculateScore(Tweet tweet, Set<String> userPreferredGenres) {
        double score = BASE_SCORE;

        // ── 1. Bônus de gênero musical ───────────────────────────
        // Verifica se algum gênero do músico-autor bate com as preferências do usuário logado
        if (tweet.getUser() != null && tweet.getUser().getMusicalGender() != null) {
            Set<String> authorGenres = extractGenreNames(tweet.getUser().getMusicalGender());
            boolean genreMatch = authorGenres.stream()
                    .anyMatch(userPreferredGenres::contains);

            if (genreMatch) {
                score += GENRE_MATCH_BONUS;
            }
        }

        // ── 2. Bônus de engajamento ──────────────────────────────
        // Curtidas e comentários mostram que a comunidade valorizou o post
        int likesCount = tweet.getLikes() != null ? tweet.getLikes().size() : 0;
        int commentsCount = tweet.getComments() != null ? tweet.getComments().size() : 0;

        score += likesCount * LIKE_BONUS;
        score += commentsCount * COMMENT_BONUS;

        // ── 3. Time Decay (penalidade de recência) ───────────────
        // Usa ChronoUnit.HOURS para calcular a idade exata do post
        long hoursOld = ChronoUnit.HOURS.between(
                tweet.getCreationTimestamp() != null ? tweet.getCreationTimestamp() : Instant.now(),
                Instant.now()
        );

        double timeDecay;
        if (hoursOld < DECAY_THRESHOLD_FRESH) {
            timeDecay = DECAY_FRESH;       // < 6h: sem penalidade
        } else if (hoursOld < DECAY_THRESHOLD_RECENT) {
            timeDecay = DECAY_RECENT;      // 6-24h: leve penalidade
        } else if (hoursOld < DECAY_THRESHOLD_OLD) {
            timeDecay = DECAY_OLD;         // 24-48h: penalidade moderada
        } else {
            timeDecay = DECAY_EXPIRED;     // > 48h: queda drástica conforme spec
        }

        return score / timeDecay;
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVADOS
    // ─────────────────────────────────────────────────────────────

    /**
     * Decide qual query usar baseado na role do usuário logado.
     * Produtores e Ouvintes veem apenas posts de Músicos.
     * Músicos veem posts de todos os músicos (incluindo eles mesmos).
     */
    private Page<Tweet> fetchTweetsByRole(String role, Pageable pageable) {
        return switch (role.toLowerCase()) {
            case "producer", "lover" -> tweetRepository.findAllByMusicians(pageable);
            case "musician"          -> tweetRepository.findAllByMusicians(pageable);
            default                  -> tweetRepository.findAllPaged(pageable);
        };
    }

    /**
     * Extrai os nomes dos gêneros de um Set<MusicalGenre> para usar na comparação.
     */
    private Set<String> extractGenreNames(Set<MusicalGenre> genres) {
        if (genres == null || genres.isEmpty()) {
            return Set.of();
        }
        return genres.stream()
                .map(MusicalGenre::getGenre)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
