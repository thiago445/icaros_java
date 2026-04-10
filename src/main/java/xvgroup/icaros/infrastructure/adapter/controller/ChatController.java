package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.ChatMessageRequest;
import xvgroup.icaros.application.dto.ChatMessageResponse;
import xvgroup.icaros.application.dto.ConversationPreview;
import xvgroup.icaros.domain.service.ChatService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // POST /chat/send - send a message
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request,
                                                           JwtAuthenticationToken token) {
        ChatMessageResponse response = chatService.sendMessage(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /chat/conversation/{partnerId} - get full conversation with a user (also marks as read)
    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(@PathVariable UUID partnerId,
                                                                     JwtAuthenticationToken token) {
        List<ChatMessageResponse> messages = chatService.getConversation(partnerId, token);
        return ResponseEntity.ok(messages);
    }

    // GET /chat/conversations - list all conversations (inbox preview)
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationPreview>> getConversationList(JwtAuthenticationToken token) {
        List<ConversationPreview> conversations = chatService.getConversationList(token);
        return ResponseEntity.ok(conversations);
    }
}
