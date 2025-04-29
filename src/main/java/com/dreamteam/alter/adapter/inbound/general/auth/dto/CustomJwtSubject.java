package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.auth.type.TokenType;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomJwtSubject {

    private String authorizationId;

    private TokenScope scope;

    private TokenType type;

    private Long userId;

    public static CustomJwtSubject of(
        String authorizationId,
        TokenScope scope,
        TokenType type,
        Long userId
    ) {
        return new CustomJwtSubject(authorizationId, scope, type, userId);
    }

}
