package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPostingApplicationListResponse {

    private Long id;

    private PostingSchedule postingSchedule;

    private Posting posting;

    private String description;

    private PostingApplicationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
