package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagerPostingDetailResponse {

    private Long id;

    private Workspace workspace;

    private String title;

    private String description;

    private int payAmount;

    private PaymentType paymentType;

    private PostingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<PostingKeyword> postingKeywords;

    private List<PostingSchedule> schedules;

    public static ManagerPostingDetailResponse of(
        Posting posting,
        List<PostingKeyword> postingKeywords
    ) {
        return new ManagerPostingDetailResponse(
            posting.getId(),
            posting.getWorkspace(),
            posting.getTitle(),
            posting.getDescription(),
            posting.getPayAmount(),
            posting.getPaymentType(),
            posting.getStatus(),
            posting.getCreatedAt(),
            posting.getUpdatedAt(),
            postingKeywords,
            posting.getSchedules()
        );
    }
}
