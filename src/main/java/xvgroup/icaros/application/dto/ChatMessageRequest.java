package xvgroup.icaros.application.dto;

import java.util.UUID;

public record ChatMessageRequest(UUID receiverId, String content) {
}
