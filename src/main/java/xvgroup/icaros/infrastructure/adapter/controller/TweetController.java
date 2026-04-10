package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xvgroup.icaros.application.dto.RequestTweet;
import xvgroup.icaros.application.dto.ResponseAllTweets;
import xvgroup.icaros.application.dto.TweetDetailResponse;
import xvgroup.icaros.domain.service.AzureStorageService;
import xvgroup.icaros.domain.service.TweetService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tweet")
public class TweetController {

    private final TweetService tweeterService;
    private final AzureStorageService azureStorageService;

    @Autowired
    public TweetController(TweetService tweeterService, AzureStorageService azureStorageService) {
        this.tweeterService = tweeterService;
        this.azureStorageService = azureStorageService;
    }

    // POST /tweet/create - create a new post (with optional media)
    @PostMapping("/create")
    public ResponseEntity<String> createTweet(@RequestPart("tweetData") RequestTweet tweetData,
                                              @RequestPart(value = "media", required = false) MultipartFile media,
                                              JwtAuthenticationToken token) {
        try {
            String mediaUrl = null;
            if (media != null && !media.isEmpty()) {
                mediaUrl = azureStorageService.uploadFile(media, "tweet-musician");
            }
            tweeterService.createTweet(tweetData, mediaUrl, token);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading media file.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /tweet/alltweets - list all posts (feed)
    @GetMapping("/alltweets")
    public ResponseEntity<List<ResponseAllTweets>> getAllTweets() {
        List<ResponseAllTweets> tweets = tweeterService.getAllTweets();
        return ResponseEntity.ok(tweets);
    }

    // GET /tweet/{tweetId} - get post detail with likes/comments
    @GetMapping("/{tweetId}")
    public ResponseEntity<TweetDetailResponse> getTweetDetail(@PathVariable Long tweetId,
                                                              JwtAuthenticationToken token) {
        TweetDetailResponse response = tweeterService.getTweetDetail(tweetId, token);
        return ResponseEntity.ok(response);
    }

    // GET /tweet/user/{userId} - list all posts from a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TweetDetailResponse>> getTweetsByUser(@PathVariable UUID userId,
                                                                     JwtAuthenticationToken token) {
        List<TweetDetailResponse> tweets = tweeterService.getTweetsByUser(userId, token);
        return ResponseEntity.ok(tweets);
    }

    // DELETE /tweet/{tweetId} - delete a post (owner only)
    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Void> deleteTweet(@PathVariable Long tweetId,
                                            JwtAuthenticationToken token) {
        tweeterService.deleteTweet(tweetId, token);
        return ResponseEntity.noContent().build();
    }
}
