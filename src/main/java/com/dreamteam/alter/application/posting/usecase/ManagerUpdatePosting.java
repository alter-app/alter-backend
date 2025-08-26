package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerUpdatePosting")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePosting implements ManagerUpdatePostingUseCase {

    private final PostingQueryRepository postingQueryRepository;
    private final PostingKeywordQueryRepository postingKeywordQueryRepository;

    @Override
    public void execute(Long postingId, UpdatePostingRequestDto request, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        Posting posting = postingQueryRepository.findByManagerAndId(postingId, managerUser)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

        // 키워드 검증
        if (ObjectUtils.isEmpty(request.getKeywords())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        List<PostingKeyword> postingKeywords = postingKeywordQueryRepository.findByIds(request.getKeywords());
        if (BooleanUtils.isFalse(postingKeywords.size() == request.getKeywords().size())) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD);
        }

        posting.updateContent(
            request.getTitle(),
            request.getDescription(),
            request.getPayAmount(),
            request.getPaymentType(),
            postingKeywords,
            request.getCreateSchedules(),
            request.getUpdateSchedules(),
            request.getDeleteScheduleIds()
        );
    }
}
