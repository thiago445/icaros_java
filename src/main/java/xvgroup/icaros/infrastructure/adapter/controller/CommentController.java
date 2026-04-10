package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.CommentRequest;
import xvgroup.icaros.application.dto.CommentResponse;
import xvgroup.icaros.domain.service.CommentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tweet")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // POST /tweet/{tweetId}/comment - add a comment to a post
    @PostMapping("/{tweetId}/comment")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long tweetId,
                                                      @RequestBody CommentRequest request,
                                                      JwtAuthenticationToken token) {
        CommentResponse response = commentService.addComment(tweetId, request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /tweet/{tweetId}/comments - list all comments of a post
    @GetMapping("/{tweetId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long tweetId) {
        List<CommentResponse> comments = commentService.getCommentsByTweet(tweetId);
        return ResponseEntity.ok(comments);
    }

    // DELETE /tweet/comment/{commentId} - delete a comment (owner only)
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId,
                                              JwtAuthenticationToken token) {
        commentService.deleteComment(commentId, token);
        return ResponseEntity.noContent().build();
    }
}
