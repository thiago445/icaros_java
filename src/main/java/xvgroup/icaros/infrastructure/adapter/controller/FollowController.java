package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.FollowResponse;
import xvgroup.icaros.application.dto.UserResponse;
import xvgroup.icaros.domain.service.FollowService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    // POST /follow/{userId} - follow or unfollow a user (toggle)
    @PostMapping("/{userId}")
    public ResponseEntity<FollowResponse> toggleFollow(@PathVariable UUID userId,
                                                       JwtAuthenticationToken token) {
        FollowResponse response = followService.toggleFollow(userId, token);
        return ResponseEntity.ok(response);
    }

    // GET /follow/{userId}/stats - get followers/following count and if you follow this user
    @GetMapping("/{userId}/stats")
    public ResponseEntity<FollowResponse> getFollowStats(@PathVariable UUID userId,
                                                         JwtAuthenticationToken token) {
        FollowResponse response = followService.getFollowStats(userId, token);
        return ResponseEntity.ok(response);
    }

    // GET /follow/{userId}/followers - list followers of a user
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable UUID userId) {
        List<UserResponse> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    // GET /follow/{userId}/following - list users that a user follows
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable UUID userId) {
        List<UserResponse> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
}
