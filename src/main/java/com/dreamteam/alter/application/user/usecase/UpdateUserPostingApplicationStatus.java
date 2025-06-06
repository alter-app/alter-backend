package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.UpdateUserPostingApplicationStatusUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("updateUserPostingApplicationStatus")
@RequiredArgsConstructor
@Transactional
public class UpdateUserPostingApplicationStatus implements UpdateUserPostingApplicationStatusUseCase {

    private final PostingApplicationQueryRepository postingApplicationQueryRepository;

    @Override
    public void execute(AppActor actor, Long applicationId, UpdateUserPostingApplicationStatusRequestDto request) {
        // 상태 변경 요청은 CANCELLED 상태로만 가능
        if (!request.getStatus().equals(PostingApplicationStatus.CANCELLED)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        User user = actor.getUser();

        PostingApplication result =
            postingApplicationQueryRepository.getUserPostingApplication(user, applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_APPLICATION_NOT_FOUND));

        // 이미 CANCELLED 상태인 경우 예외 발생
        if (result.getStatus()
            .equals(PostingApplicationStatus.CANCELLED)) {
            throw new CustomException(ErrorCode.POSTING_APPLICATION_ALREADY_CANCELLED);
        }

        result.updateStatus(request.getStatus());
    }

}
