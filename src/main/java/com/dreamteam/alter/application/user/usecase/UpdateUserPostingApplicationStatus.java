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

        switch (result.getStatus()) {
            case SUBMITTED, SHORTLISTED -> result.updateStatus(request.getStatus());
            case CANCELLED -> throw new CustomException(ErrorCode.POSTING_APPLICATION_ALREADY_CANCELLED);
            case ACCEPTED -> throw new CustomException(ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE, "합격한 지원서는 취소할 수 없습니다.");
            default -> throw new CustomException(ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);
        }
    }

}
