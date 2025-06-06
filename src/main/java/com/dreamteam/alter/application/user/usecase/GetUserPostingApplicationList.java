package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UserPostingApplicationListResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.UserPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.GetUserPostingApplicationListUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("getUserPostingApplicationList")
@RequiredArgsConstructor
@Transactional
public class GetUserPostingApplicationList implements GetUserPostingApplicationListUseCase {

    private final PostingApplicationQueryRepository postingApplicationQueryRepository;

    @Override
    public PaginatedResponseDto<UserPostingApplicationListResponseDto> execute(
        AppActor actor,
        PageRequestDto pageRequest
    ) {
        User user = actor.getUser();

        long count = postingApplicationQueryRepository.getCountByUser(user);
        PageResponseDto pageResponseDto = PageResponseDto.of(pageRequest, (int) count);
        if (count == 0) {
            return PaginatedResponseDto.empty(pageResponseDto);
        }

        List<UserPostingApplicationListResponse> result =
            postingApplicationQueryRepository.getUserPostingApplicationList(user, pageRequest);

        return PaginatedResponseDto.of(
            pageResponseDto,
            result.stream()
                .map(UserPostingApplicationListResponseDto::from)
                .toList()
        );
    }

}
