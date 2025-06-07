package com.dreamteam.alter.adapter.outbound.user.persistence.readonly;

import com.dreamteam.alter.domain.posting.entity.Posting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoritePostingListResponse {

    private Long id;

    private Posting posting;

    private LocalDateTime createdAt;

}
