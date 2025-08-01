package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerPostingApplicationDetailResponse {

    private Long id;

    private PostingApplicationWorkspaceResponse workspace;

    private PostingSchedule schedule;

    private String description;

    private PostingApplicationStatus status;

    private User user;

    private LocalDateTime createdAt;

}
