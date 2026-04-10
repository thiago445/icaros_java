package xvgroup.icaros.domain.ports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xvgroup.icaros.domain.entity.ProfileUser;
import xvgroup.icaros.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileUserRepository extends JpaRepository<ProfileUser,UUID> {


    Optional<ProfileUser> findByUser(User user);
}
