package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestListRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestListResponseDto;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.reputation.port.inbound.GetReputationRequestListUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getReputationRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetReputationRequestList implements GetReputationRequestListUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ReputationRequestListResponseDto> execute(
        ReputationRequestListRequestDto request,
        CursorPageRequestDto pageRequest
    ) {
        if (ReputationType.WORKSPACE.equals(request.getTargetType())) {
            Workspace workspace = workspaceQueryRepository.findById(request.getTargetId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

            if (ObjectUtils.isEmpty(workspaceQueryRepository.getByManagerUserAndId(workspace.getManagerUser(), request.getTargetId()))) {
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "관리중인 업장이 아닙니다.");
            }
        }

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        long count = reputationRequestQueryRepository.getCountOfReputationRequestsByTarget(
            request.getTargetType(),
            request.getTargetId()
        );
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        List<ReputationRequestListResponse> reputationRequests =
            reputationRequestQueryRepository.getReputationRequestsWithCursor(
                cursorPageRequest,
                request.getTargetType(),
                request.getTargetId()
            );
        if (ObjectUtils.isEmpty(reputationRequests)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), (int) count));
        }

        ReputationRequestListResponse last = reputationRequests.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            cursorPageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            reputationRequests.stream()
                .map(ReputationRequestListResponseDto::of)
                .toList()
        );
    }

}
