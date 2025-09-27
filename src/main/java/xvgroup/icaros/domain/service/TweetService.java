package xvgroup.icaros.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.stylesheets.LinkStyle;
import xvgroup.icaros.application.dto.RequestTweet;
import xvgroup.icaros.application.dto.ResponseAllTweets;
import xvgroup.icaros.domain.entity.Tweet;
import xvgroup.icaros.domain.entity.User;
import xvgroup.icaros.domain.ports.TweetRepository;
import xvgroup.icaros.domain.ports.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TweetService {
    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;

    @Autowired
    public TweetService(UserRepository userRepository, TweetRepository tweetRepository) {
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    public void createTweet(RequestTweet tweetData, String mediaUrl, JwtAuthenticationToken token) {
        User creator= userRepository.findById(UUID.fromString(token.getToken().getSubject()))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet tweet = new Tweet();
        tweet.setUser(creator);
        tweet.setMessageContent(tweetData.messageContent());
        tweet.setTitle(tweetData.title());
        tweet.setMediaUrl(mediaUrl);

        tweetRepository.save(tweet);

    }

    public List<ResponseAllTweets> getAllTweets() {
        List<Tweet> twwets=tweetRepository.findAll();


        List<ResponseAllTweets> tweetsResponse= twwets.stream().map(value ->ResponseAllTweets.toResponseAllTweets(value)).toList() ;
        return tweetsResponse;


    }
}

