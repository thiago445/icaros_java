package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.CommentRequest;
import xvgroup.icaros.application.dto.CommentResponse;
import xvgroup.icaros.domain.entity.Comment;
import xvgroup.icaros.domain.entity.Notification;
import xvgroup.icaros.domain.entity.Tweet;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.CommentRepository;
import xvgroup.icaros.domain.ports.NotificationRepository;
import xvgroup.icaros.domain.ports.TweetRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public CommentService(CommentRepository commentRepository,
                          TweetRepository tweetRepository,
                          UserRepository userRepository,
                          NotificationRepository notificationRepository) {
        this.commentRepository = commentRepository;
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public CommentResponse addComment(Long tweetId, CommentRequest request, JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Comment comment = new Comment();
        comment.setText(request.text());
        comment.setTweet(tweet);
        comment.setAuthor(author);

        commentRepository.save(comment);

        if (!tweet.getUser().getUserId().equals(userId)) {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.COMMENT);
            notification.setMessage(author.getName() + " commented on your post");
            notification.setRecipient(tweet.getUser());
            notification.setActor(author);
            notificationRepository.save(notification);
        }

        return CommentResponse.fromEntity(comment);
    }

    public List<CommentResponse> getCommentsByTweet(Long tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        return commentRepository.findByTweetOrderByCreationTimestampAsc(tweet).stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteComment(UUID commentId, JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}
