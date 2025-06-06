package com.dreamteam.alter.domain.user.context;

import com.dreamteam.alter.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActor {

    private Long userId;

    private User user;

    private List<? extends GrantedAuthority> authorities;

    public static AdminActor from(User user, List<? extends GrantedAuthority> authorities) {
        if (ObjectUtils.isEmpty(user)) {
            return null;
        }

        return new AdminActor(user.getId(), user, authorities);
    }

}
