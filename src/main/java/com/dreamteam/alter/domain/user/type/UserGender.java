package com.dreamteam.alter.domain.user.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum UserGender {
    GENDER_MALE("남성"),
    GENDER_FEMALE("여성")
    ;

    private final String description;

    private static final Map<UserGender, String> DESCRIPTION_MAP = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(g -> g, UserGender::getDescription));

    public static Map<UserGender, String> describe() {
        return DESCRIPTION_MAP;
    }
}
