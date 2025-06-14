package com.dreamteam.alter.domain.user.context;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerActor {

    private Long managerId;

    private ManagerUser managerUser;

    private List<? extends GrantedAuthority> authorities;

    public static ManagerActor from(ManagerUser managerUser, List<? extends GrantedAuthority> authorities) {
        if (ObjectUtils.isEmpty(managerUser)) {
            return null;
        }
        return new ManagerActor(managerUser.getId(), managerUser, authorities);
    }
}
