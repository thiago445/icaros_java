package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.ChatMessage;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        String content,
        UserResponse sender,
        UserResponse receiver,
        boolean read,
        Instant sentAt
) {
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getContent(),
                UserResponse.fromEntity(message.getSender()),
                UserResponse.fromEntity(message.getReceiver()),
                message.isRead(),
                message.getSentAt()
        );
    }
}
