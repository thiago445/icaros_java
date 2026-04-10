package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.Tweet;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TweetDetailResponse(
        Long id,
        String title,
        String messageContent,
        String mediaUrl,
        Instant creationTimestamp,
        UserResponse creator,
        long likesCount,
        boolean likedByMe,
        List<CommentResponse> comments
) {
    public static TweetDetailResponse fromEntity(Tweet tweet, UUID currentUserId) {
        boolean liked = currentUserId != null
                && tweet.getLikes() != null
                && tweet.getLikes().stream().anyMatch(u -> u.getUserId().equals(currentUserId));

        List<CommentResponse> commentList = tweet.getComments() != null
                ? tweet.getComments().stream().map(CommentResponse::fromEntity).toList()
                : java.util.Collections.emptyList();

        long likesCount = tweet.getLikes() != null ? tweet.getLikes().size() : 0;

        return new TweetDetailResponse(
                tweet.getId(),
                tweet.getTitle(),
                tweet.getMessageContent(),
                tweet.getMediaUrl(),
                tweet.getCreationTimestamp(),
                UserResponse.fromEntity(tweet.getUser()),
                likesCount,
                liked,
                commentList
        );
    }
}
