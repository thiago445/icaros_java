package xvgroup.icaros.application.dto;

import java.util.UUID;

public record FollowResponse(
        long followers,
        long following,
        boolean isFollowing
) {
}
