package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.UserPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PostingApplicationQueryRepository {
    long getCountByUser(User user);
    List<UserPostingApplicationListResponse> getUserPostingApplicationList(User user, PageRequestDto pageRequest);
    Optional<PostingApplication> getUserPostingApplication(User user, Long applicationId);
}
