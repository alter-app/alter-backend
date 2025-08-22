package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class KeywordFrequency {
    
    private String keywordId;
    private String keywordName;
    private String keywordDescription;
    private Integer count;
    private Double percentage;
    private List<String> userDescriptions;
    
    public static KeywordFrequency of(
        String keywordId,
        String keywordName,
        String keywordDescription,
        Integer count,
        Double percentage,
        List<String> userDescriptions
    ) {
        return KeywordFrequency.builder()
            .keywordId(keywordId)
            .keywordName(keywordName)
            .keywordDescription(keywordDescription)
            .count(count)
            .percentage(percentage)
            .userDescriptions(userDescriptions)
            .build();
    }
}
