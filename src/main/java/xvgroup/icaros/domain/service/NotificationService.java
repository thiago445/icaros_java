package xvgroup.icaros.domain.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.NotificationResponse;
import xvgroup.icaros.domain.entity.Notification;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.NotificationRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<NotificationResponse> getMyNotifications(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    public long countUnread(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Transactional
    public void markAllAsRead(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Notification> unread = notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
                .stream()
                .filter(n -> !n.isRead())
                .toList();

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markAsRead(UUID notificationId, JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getToken().getSubject());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getRecipient().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
