package com.dreamteam.alter.domain.posting.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostingSortType {
    PAY_AMOUNT("급여순")
    ;

    private final String description;
}
