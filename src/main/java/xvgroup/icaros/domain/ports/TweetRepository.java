package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.Tweet;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {
}
