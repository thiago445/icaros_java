package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.FollowResponse;
import xvgroup.icaros.application.dto.UserResponse;
import xvgroup.icaros.domain.entity.Follow;
import xvgroup.icaros.domain.entity.Notification;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.FollowRepository;
import xvgroup.icaros.domain.ports.NotificationRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public FollowService(FollowRepository followRepository,
                         UserRepository userRepository,
                         NotificationRepository notificationRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public FollowResponse toggleFollow(UUID targetUserId, JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getToken().getSubject());

        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }

        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User followed = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        boolean alreadyFollowing = followRepository.existsByFollowerAndFollowed(follower, followed);

        if (alreadyFollowing) {
            followRepository.findByFollowerAndFollowed(follower, followed)
                    .ifPresent(followRepository::delete);
        } else {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowed(followed);
            followRepository.save(follow);

            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.FOLLOW);
            notification.setMessage(follower.getName() + " started following you");
            notification.setRecipient(followed);
            notification.setActor(follower);
            notificationRepository.save(notification);
        }

        long followersCount = followRepository.countByFollowed(followed);
        long followingCount = followRepository.countByFollower(followed);

        return new FollowResponse(followersCount, followingCount, !alreadyFollowing);
    }

    public FollowResponse getFollowStats(UUID targetUserId, JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getToken().getSubject());

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        long followersCount = followRepository.countByFollowed(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);
        boolean isFollowing = followRepository.existsByFollowerAndFollowed(currentUser, targetUser);

        return new FollowResponse(followersCount, followingCount, isFollowing);
    }

    public List<UserResponse> getFollowers(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return followRepository.findByFollowed(user).stream()
                .map(f -> UserResponse.fromEntity(f.getFollower()))
                .toList();
    }

    public List<UserResponse> getFollowing(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return followRepository.findByFollower(user).stream()
                .map(f -> UserResponse.fromEntity(f.getFollowed()))
                .toList();
    }
}
