package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.Keyword;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class PostingListResponse {

    private Long id;

    private String title;

    private int payAmount;

    private PaymentType paymentType;

    private LocalDateTime createdAt;

    private List<Keyword> keywords;

    private List<PostingSchedule> schedules;

    public static PostingListResponse of(Posting posting, Map<Long, List<Keyword>> keywordsMap) {
        return PostingListResponse.builder()
            .id(posting.getId())
            .title(posting.getTitle())
            .payAmount(posting.getPayAmount())
            .paymentType(posting.getPaymentType())
            .createdAt(posting.getCreatedAt())
            .schedules(posting.getSchedules())
            .keywords(keywordsMap.get(posting.getId()))
            .build();
    }

}
