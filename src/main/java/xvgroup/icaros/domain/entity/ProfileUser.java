    package xvgroup.icaros.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID profileId;
    private String urlProfile;
    private String nickName;
    private String coverPhotoUrl;
    private String bio;
    private String city;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
