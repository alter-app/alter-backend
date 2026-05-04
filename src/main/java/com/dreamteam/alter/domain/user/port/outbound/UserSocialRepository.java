package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.domain.user.entity.UserSocial;

public interface UserSocialRepository {
    void delete(UserSocial userSocial);
}
