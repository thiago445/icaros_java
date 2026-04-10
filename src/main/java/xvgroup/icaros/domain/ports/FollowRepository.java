package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.Follow;
import xvgroup.icaros.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerAndFollowed(User follower, User followed);

    Optional<Follow> findByFollowerAndFollowed(User follower, User followed);

    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowed(User followed);

    long countByFollowed(User followed);

    long countByFollower(User follower);
}
