package xvgroup.icaros.domain.service;


import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.ChatMessageRequest;
import xvgroup.icaros.application.dto.ChatMessageResponse;
import xvgroup.icaros.application.dto.ConversationPreview;
import xvgroup.icaros.application.dto.UserResponse;
import xvgroup.icaros.domain.entity.ChatMessage;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.ChatMessageRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, JwtAuthenticationToken token) {
        UUID senderId = UUID.fromString(token.getToken().getSubject());

        if (senderId.equals(request.receiverId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot send a message to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.content());

        // NOVO: Processa a citação se o replyToId for enviado
        if (request.replyToId() != null) {
            ChatMessage repliedMessage = chatMessageRepository.findById(request.replyToId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Replied message not found"));
            message.setReplyTo(repliedMessage);
        }

        chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);

        // Notifica o destinatário no canal exclusivo dele
        messagingTemplate.convertAndSend("/topic/messages/" + receiver.getUserId(), response);

        return response;
    }

    @Transactional
    public List<ChatMessageResponse> getConversation(UUID partnerId, JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getToken().getSubject());

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));

        List<ChatMessage> messages = chatMessageRepository.findConversation(currentUser, partner);

        // Mark unread messages as read
        messages.stream()
                .filter(m -> m.getReceiver().getUserId().equals(currentUserId) && !m.isRead())
                .forEach(m -> m.setRead(true));

        chatMessageRepository.saveAll(messages);

        return messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .toList();
    }

    public List<ConversationPreview> getConversationList(JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getToken().getSubject());

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<User> partners = chatMessageRepository.findConversationPartners(currentUser);

        return partners.stream().map(partner -> {
            List<ChatMessage> conv = chatMessageRepository.findConversation(currentUser, partner);
            long unread = chatMessageRepository.countBySenderAndReceiverAndReadFalse(partner, currentUser);

            ChatMessage last = conv.isEmpty() ? null : conv.get(conv.size() - 1);

            return new ConversationPreview(
                    UserResponse.fromEntity(partner),
                    last != null ? last.getContent() : "",
                    last != null ? last.getSentAt() : null,
                    unread
            );
        }).toList();
    }


}
