package xvgroup.icaros.infrastructure.adapter.controller;

import com.azure.core.annotation.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.stylesheets.LinkStyle;
import xvgroup.icaros.application.dto.RequestTweet;
import xvgroup.icaros.application.dto.ResponseAllTweets;
import xvgroup.icaros.domain.entity.Tweet;
import xvgroup.icaros.domain.service.TweetService;
import xvgroup.icaros.domain.service.AzureStorageService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController()
@RequestMapping("/tweet")
public class TweetController {
    private final TweetService tweeterService;
    private final AzureStorageService azureStorageService;

    @Autowired
    public TweetController(TweetService tweeterService, AzureStorageService azureStorageService) {
        this.tweeterService = tweeterService;
        this.azureStorageService = azureStorageService;
    }

    @PostMapping("/create")
    ResponseEntity<String> createTweet(@RequestPart("tweetData") RequestTweet tweetData,
                                     @RequestPart(value = "media", required = false)MultipartFile media,
                                     JwtAuthenticationToken token){
        try {
            String mediaUrl= null;
            if(!media.isEmpty()){
                mediaUrl=  azureStorageService.uploadFile(media, "tweet-musician");
            }
            tweeterService.createTweet(tweetData, mediaUrl,token);



        }catch (IOException e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error in upload of anybody.");

        }

        return ResponseEntity.ok().build();

    }

    @GetMapping("/alltweets")
    ResponseEntity<List<ResponseAllTweets>> getAllTweets(){
        List<ResponseAllTweets> twwets = tweeterService.getAllTweets();
        return ResponseEntity.ok().body(twwets);
    }
}
