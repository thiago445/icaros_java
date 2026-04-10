package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.Notification;
import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndReadFalse(User recipient);
}
