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
        Instant sentAt,
        ReplyPreview replyTo
) {
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        
        ReplyPreview replyPreview = null;
        if (message.getReplyTo() != null) {
            // CORREÇÃO: Usando apenas getName() porque a entidade User não tem nickName
            replyPreview = new ReplyPreview(
                    message.getReplyTo().getId(),
                    message.getReplyTo().getContent(),
                    message.getReplyTo().getSender().getName() 
            );
        }

        return new ChatMessageResponse(
                message.getId(),
                message.getContent(),
                UserResponse.fromEntity(message.getSender()),
                UserResponse.fromEntity(message.getReceiver()),
                message.isRead(),
                message.getSentAt(),
                replyPreview
        );
    }
}

// Sub-record para enviar apenas o essencial da mensagem citada pro frontend
record ReplyPreview(
        UUID id, 
        String content, 
        String senderName
) {}