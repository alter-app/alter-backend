package com.dreamteam.alter.domain.user.type;

import java.util.Map;

public enum UserGender {
    GENDER_MALE,
    GENDER_FEMALE
    ;

    public static Map<UserGender, String> describe() {
        return Map.of(
            UserGender.GENDER_MALE, "남성",
            UserGender.GENDER_FEMALE, "여성"
        );
    }
}
