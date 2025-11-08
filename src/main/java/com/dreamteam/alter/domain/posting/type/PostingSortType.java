package com.dreamteam.alter.domain.posting.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum PostingSortType {
    PAY_AMOUNT,
    LATEST
    ;

    public static Map<PostingSortType, String> describe() {
        return Map.of(
            PostingSortType.PAY_AMOUNT, "급여순",
            PostingSortType.LATEST, "최신 등록 순"
        );
    }
}
