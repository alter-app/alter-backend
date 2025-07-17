package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerPostingApplicationListResponse {

    private Long id;

    private PostingApplicationWorkspaceResponse workspace;

    private PostingSchedule schedule;

    private PostingApplicationStatus status;

    private LocalDateTime createdAt;

}
