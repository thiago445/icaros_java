package xvgroup.icaros.application.dto;

import java.time.Instant;

public record ConversationPreview(
        UserResponse partner,
        String lastMessage,
        Instant lastMessageAt,
        long unreadCount
) {
}
