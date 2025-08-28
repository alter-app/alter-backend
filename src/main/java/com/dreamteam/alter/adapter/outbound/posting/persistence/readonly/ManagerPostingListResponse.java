package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ManagerPostingListResponse {

    private Long id;

    private String title;

    private int payAmount;

    private PaymentType paymentType;

    private LocalDateTime createdAt;

    private List<PostingKeyword> postingKeywords;

    private List<PostingSchedule> schedules;

    private Workspace workspace;

    public static ManagerPostingListResponse of(Posting posting, Map<Long, List<PostingKeyword>> keywordsMap) {
        return ManagerPostingListResponse.builder()
            .id(posting.getId())
            .title(posting.getTitle())
            .payAmount(posting.getPayAmount())
            .paymentType(posting.getPaymentType())
            .createdAt(posting.getCreatedAt())
            .schedules(posting.getSchedules())
            .postingKeywords(keywordsMap.get(posting.getId()))
            .workspace(posting.getWorkspace())
            .build();
    }

}
