package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerGetPostingApplicationDetailUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("managerGetPostingApplicationDetail")
@RequiredArgsConstructor
@Transactional
public class ManagerGetPostingApplicationDetail implements ManagerGetPostingApplicationDetailUseCase {

    private final PostingApplicationQueryRepository postingApplicationQueryRepository;

    @Override
    public PostingApplicationResponseDto execute(
        Long postingApplicationId,
        ManagerActor actor
    ) {
        ManagerUser managerUser = actor.getManagerUser();

        return PostingApplicationResponseDto.from(
            postingApplicationQueryRepository.getManagerPostingApplicationDetail(managerUser, postingApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_APPLICATION_NOT_FOUND)));
    }

}
