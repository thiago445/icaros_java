package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.domain.service.LikeService;

import java.util.Map;

@RestController
@RequestMapping("/tweet")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // POST /tweet/{tweetId}/like - like or unlike a post (toggle)
    @PostMapping("/{tweetId}/like")
    public ResponseEntity<Map<String, Long>> toggleLike(@PathVariable Long tweetId,
                                                        JwtAuthenticationToken token) {
        long totalLikes = likeService.toggleLike(tweetId, token);
        return ResponseEntity.ok(Map.of("likes", totalLikes));
    }
}
