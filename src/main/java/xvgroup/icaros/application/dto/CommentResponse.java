package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.Comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String text,
        Instant creationTimestamp,
        UserResponse author
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getCreationTimestamp(),
                UserResponse.fromEntity(comment.getAuthor())
        );
    }
}
