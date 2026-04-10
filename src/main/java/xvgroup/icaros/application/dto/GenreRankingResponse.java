package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.GenreRanking;

import java.time.Instant;

public record GenreRankingResponse(
        int position,
        String genre,
        long totalLikes,
        long totalComments,
        long totalPosts,
        long totalMusicians,
        double rankingScore,
        Instant lastUpdated
) {
    public static GenreRankingResponse fromEntity(int position, GenreRanking r) {
        return new GenreRankingResponse(
                position,
                r.getGenre().getGenre(),
                r.getTotalLikes(),
                r.getTotalComments(),
                r.getTotalPosts(),
                r.getTotalMusicians(),
                Math.round(r.getRankingScore() * 100.0) / 100.0,
                r.getLastUpdated()
        );
    }
}
