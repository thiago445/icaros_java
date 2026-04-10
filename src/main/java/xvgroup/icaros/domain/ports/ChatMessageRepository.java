package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.ChatMessage;
import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :userA AND m.receiver = :userB) OR " +
            "(m.sender = :userB AND m.receiver = :userA) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(@Param("userA") User userA, @Param("userB") User userB);


    // ✅ QUERY CORRIGIDA 100% PARA O HIBERNATE 6 (Comparando os IDs)
    @Query("SELECT DISTINCT u FROM User u WHERE " +
            "u.userId IN (SELECT m.receiver.userId FROM ChatMessage m WHERE m.sender = :user) OR " +
            "u.userId IN (SELECT m.sender.userId FROM ChatMessage m WHERE m.receiver = :user)")
    List<User> findConversationPartners(@Param("user") User user);

    long countBySenderAndReceiverAndReadFalse(User sender, User receiver);
}