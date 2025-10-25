package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.type.UserRole;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginUserDto {

    private String authorizationId;

    private TokenScope scope;

    private Long id;

    private String email;

    private UserRole role;

    public static LoginUserDto of(Authorization authorization, UserRole role) {
        return LoginUserDto.builder()
            .authorizationId(authorization.getId())
            .scope(authorization.getScope())
            .id(authorization.getUser().getId())
            .email(authorization.getUser().getEmail())
            .role(role)
            .build();
    }

}
