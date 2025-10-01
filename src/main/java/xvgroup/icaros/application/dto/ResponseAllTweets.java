package xvgroup.icaros.application.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import xvgroup.icaros.domain.entity.Comment;
import xvgroup.icaros.domain.entity.Tweet;
import xvgroup.icaros.domain.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ResponseAllTweets (String title,
                                 String messageContent,
                                 String mediaUrl,
                                 Instant creationTimesTamp,
                                 UserResponse Creator){


    public static ResponseAllTweets toResponseAllTweets(Tweet value) {
        return new ResponseAllTweets(value.getTitle(),
                value.getMessageContent(),
                value.getMediaUrl(),
                value.getCreationTimestamp(),
                UserResponse.fromEntity(value.getUser())
        );
    }
}
