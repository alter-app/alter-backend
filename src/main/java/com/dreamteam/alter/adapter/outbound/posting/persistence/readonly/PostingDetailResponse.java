package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostingDetailResponse {

    private Long id;

    private Workspace workspace;

    private String title;

    private String description;

    private int payAmount;

    private PaymentType paymentType;

    private LocalDateTime createdAt;

    private List<PostingKeyword> postingKeywords;

    private List<PostingSchedule> schedules;

    public static PostingDetailResponse of(Posting posting, List<PostingKeyword> postingKeywords) {
        return new PostingDetailResponse(
            posting.getId(),
            posting.getWorkspace(),
            posting.getTitle(),
            posting.getDescription(),
            posting.getPayAmount(),
            posting.getPaymentType(),
            posting.getCreatedAt(),
            postingKeywords,
            posting.getSchedules()
        );
    }

}
