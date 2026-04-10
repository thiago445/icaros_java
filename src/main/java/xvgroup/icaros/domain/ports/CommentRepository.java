package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.Comment;
import xvgroup.icaros.domain.entity.Tweet;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTweetOrderByCreationTimestampAsc(Tweet tweet);

    long countByTweet(Tweet tweet);
}
