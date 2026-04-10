package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.GenreRankingResponse;
import xvgroup.icaros.domain.entity.*;
import xvgroup.icaros.domain.ports.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Calcula o ranking de gêneros musicais da plataforma.
 *
 * FÓRMULA DO RANKING:
 *   score = (likes × 1.0) + (comentários × 2.0) + (posts × 0.5) + (músicos × 3.0)
 *
 * Peso maior para músicos (3.0) porque indica tamanho da cena do gênero.
 * Peso maior para comentários (2.0) porque indica engajamento qualitativo.
 * Curtidas (1.0) e posts (0.5) somam mas com menor impacto.
 */
@Service
public class GenreRankingService {

    private static final double WEIGHT_LIKES     = 1.0;
    private static final double WEIGHT_COMMENTS  = 2.0;
    private static final double WEIGHT_POSTS     = 0.5;
    private static final double WEIGHT_MUSICIANS = 3.0;

    private final GenreRankingRepository rankingRepository;
    private final MusicalGenreRepository musicalGenreRepository;
    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;

    public GenreRankingService(GenreRankingRepository rankingRepository,
                                MusicalGenreRepository musicalGenreRepository,
                                UserRepository userRepository,
                                TweetRepository tweetRepository) {
        this.rankingRepository = rankingRepository;
        this.musicalGenreRepository = musicalGenreRepository;
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    // ── Retorna o ranking atual (cached) ─────────────────────────
    public List<GenreRankingResponse> getRanking() {
        List<GenreRanking> ranking = rankingRepository.findAllOrderByScore();

        if (ranking.isEmpty()) {
            // Se nunca foi calculado, calcula agora
            refreshRanking();
            ranking = rankingRepository.findAllOrderByScore();
        }

        AtomicInteger position = new AtomicInteger(1);
        return ranking.stream()
                .map(r -> GenreRankingResponse.fromEntity(position.getAndIncrement(), r))
                .collect(Collectors.toList());
    }

    // ── Recalcula o ranking completo ─────────────────────────────
    @Transactional
    public List<GenreRankingResponse> refreshRanking() {
        List<MusicalGenre> allGenres = musicalGenreRepository.findAll();
        List<User> allMusicians = userRepository.findAllMusicians();
        List<Tweet> allTweets = tweetRepository.findAll();

        for (MusicalGenre genre : allGenres) {

            // Músicos deste gênero
            List<User> genreMusicians = allMusicians.stream()
                    .filter(m -> m.getMusicalGender() != null &&
                                 m.getMusicalGender().stream()
                                   .anyMatch(g -> g.getGenre().equalsIgnoreCase(genre.getGenre())))
                    .collect(Collectors.toList());

            Set<UUID> genreMusicianIds = genreMusicians.stream()
                    .map(User::getUserId)
                    .collect(Collectors.toSet());

            // Posts deste gênero (posts de músicos que tocam este gênero)
            List<Tweet> genrePosts = allTweets.stream()
                    .filter(t -> t.getUser() != null &&
                                 genreMusicianIds.contains(t.getUser().getUserId()))
                    .collect(Collectors.toList());

            long totalLikes    = genrePosts.stream().mapToLong(t -> t.getLikes() != null ? t.getLikes().size() : 0).sum();
            long totalComments = genrePosts.stream().mapToLong(t -> t.getComments() != null ? t.getComments().size() : 0).sum();
            long totalPosts    = genrePosts.size();
            long totalMusicians = genreMusicians.size();

            double score = (totalLikes    * WEIGHT_LIKES)
                         + (totalComments * WEIGHT_COMMENTS)
                         + (totalPosts    * WEIGHT_POSTS)
                         + (totalMusicians * WEIGHT_MUSICIANS);

            GenreRanking ranking = rankingRepository.findByGenre(genre)
                    .orElse(new GenreRanking());

            ranking.setGenre(genre);
            ranking.setTotalLikes(totalLikes);
            ranking.setTotalComments(totalComments);
            ranking.setTotalPosts(totalPosts);
            ranking.setTotalMusicians(totalMusicians);
            ranking.setRankingScore(score);

            rankingRepository.save(ranking);
        }

        return getRanking();
    }
}
