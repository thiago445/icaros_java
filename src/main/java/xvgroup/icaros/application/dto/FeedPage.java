package xvgroup.icaros.application.dto;

import java.util.List;

/**
 * Resposta paginada do feed.
 * Contém os posts da página atual mais metadados de navegação.
 */
public record FeedPage(
        List<TweetDetailResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
