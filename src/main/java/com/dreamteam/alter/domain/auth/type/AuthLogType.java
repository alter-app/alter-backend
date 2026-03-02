package com.dreamteam.alter.domain.auth.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum AuthLogType {
    LOGIN("로그인"),
    TOKEN_REISSUE("토큰 재발급")
    ;

    private final String description;

    private static final Map<AuthLogType, String> DESCRIPTIONS =
        Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(v -> v, AuthLogType::getDescription));

    public static Map<AuthLogType, String> describe() {
        return DESCRIPTIONS;
    }
}
