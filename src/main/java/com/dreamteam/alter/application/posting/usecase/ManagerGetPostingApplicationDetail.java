package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingApplicationDetailResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerGetPostingApplicationDetailUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationSummaryQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
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
    private final ReputationSummaryQueryRepository reputationSummaryQueryRepository;

    @Override
    public PostingApplicationResponseDto execute(
        Long postingApplicationId,
        ManagerActor actor
    ) {
        ManagerUser managerUser = actor.getManagerUser();

        ManagerPostingApplicationDetailResponse applicationDetail = postingApplicationQueryRepository
            .getManagerPostingApplicationDetail(managerUser, postingApplicationId)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_APPLICATION_NOT_FOUND));

        // 지원자 평판 요약 정보 조회
        ReputationSummaryDto applicantReputationSummary = reputationSummaryQueryRepository
            .findByTarget(ReputationType.USER, applicationDetail.getUser().getId())
            .map(ReputationSummaryDto::from)
            .orElse(null);

        return PostingApplicationResponseDto.fromWithReputationSummary(applicationDetail, applicantReputationSummary);
    }

}
