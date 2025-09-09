package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class PostingFilterOptionsResponse {

    private List<String> provinces;
    private List<String> districts;
    private List<String> towns;

    public static PostingFilterOptionsResponse of(
            List<String> provinces,
            List<String> districts,
            List<String> towns
    ) {
        return PostingFilterOptionsResponse.builder()
                .provinces(provinces)
                .districts(districts)
                .towns(towns)
                .build();
    }

}
