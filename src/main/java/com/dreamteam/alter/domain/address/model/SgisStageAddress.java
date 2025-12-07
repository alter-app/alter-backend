package com.dreamteam.alter.domain.address.model;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class SgisStageAddress {

    private String code;
    private String name;

    public static SgisStageAddress of(String code, String name) {
        return SgisStageAddress.builder()
            .code(code)
            .name(name)
            .build();
    }
}
