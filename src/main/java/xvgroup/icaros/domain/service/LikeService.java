package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.domain.entity.Notification;
import xvgroup.icaros.domain.entity.Tweet;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.NotificationRepository;
import xvgroup.icaros.domain.ports.TweetRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.UUID;

@Service
public class LikeService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public LikeService(TweetRepository tweetRepository,
                       UserRepository userRepository,
                       NotificationRepository notificationRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public long toggleLike(Long tweetId, JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet tweet = tweetRepository.findByIdWithLikes(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        boolean alreadyLiked = tweet.getLikes().stream()
                .anyMatch(u -> u.getUserId().equals(userId));

        if (alreadyLiked) {
            tweet.getLikes().removeIf(u -> u.getUserId().equals(userId));
        } else {
            tweet.getLikes().add(user);

            if (!tweet.getUser().getUserId().equals(userId)) {
                Notification notification = new Notification();
                notification.setType(Notification.NotificationType.LIKE);
                notification.setMessage(user.getName() + " liked your post");
                notification.setRecipient(tweet.getUser());
                notification.setActor(user);
                notificationRepository.save(notification);
            }
        }

        tweetRepository.save(tweet);
        return tweet.getLikes().size();
    }
}
