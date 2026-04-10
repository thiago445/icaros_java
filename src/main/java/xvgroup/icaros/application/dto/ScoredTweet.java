package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.Tweet;

/**
 * Wrapper interno usado pelo FeedService para carregar um Tweet
 * junto com o score calculado antes de ordenar o feed.
 * Não é exposto ao frontend — apenas usado internamente nos streams.
 */
public record ScoredTweet(Tweet tweet, double score) implements Comparable<ScoredTweet> {

    @Override
    public int compareTo(ScoredTweet other) {
        // Ordenação decrescente: maior score primeiro
        return Double.compare(other.score, this.score);
    }
}
