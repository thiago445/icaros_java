package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.ProfileUser;
import xvgroup.icaros.domain.entity.User;

import java.util.UUID;

public record RequestProfile(
                             String nickName,
                             String bio,
                             String city
                             ) {

}
