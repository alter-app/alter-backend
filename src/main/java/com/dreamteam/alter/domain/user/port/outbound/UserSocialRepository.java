package com.dreamteam.alter.domain.user.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.user.entity.UserSocial;

public interface UserSocialRepository {
    void delete(UserSocial userSocial);

    void deleteAll(List<UserSocial> userSocials);
}
