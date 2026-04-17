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
        ReplyPreview replyTo // NOVO: Campo opcional para a citação
) {
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        
        // NOVO: Verifica se existe uma mensagem sendo respondida
        ReplyPreview replyPreview = null;
        if (message.getReplyTo() != null) {
            String nicknameOrName = message.getReplyTo().getSender().getNickName() != null 
                    ? message.getReplyTo().getSender().getNickName() 
                    : message.getReplyTo().getSender().getName();

            replyPreview = new ReplyPreview(
                    message.getReplyTo().getId(),
                    message.getReplyTo().getContent(),
                    nicknameOrName
            );
        }

        return new ChatMessageResponse(
                message.getId(),
                message.getContent(),
                UserResponse.fromEntity(message.getSender()),
                UserResponse.fromEntity(message.getReceiver()),
                message.isRead(),
                message.getSentAt(),
                replyPreview // Adiciona o preview aqui
        );
    }
}

// NOVO: Sub-record que envia apenas o essencial da mensagem citada pro frontend
record ReplyPreview(
        UUID id, 
        String content, 
        String senderName
) {}