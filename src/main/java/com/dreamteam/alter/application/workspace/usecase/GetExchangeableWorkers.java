package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.ExchangeableWorkerResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.inbound.GetExchangeableWorkersUseCase;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("getExchangeableWorkers")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetExchangeableWorkers implements GetExchangeableWorkersUseCase {

    private final WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ExchangeableWorkerResponseDto> execute(
        AppActor actor,
        Long scheduleId,
        CursorPageRequestDto pageRequestDto
    ) {
        // 스케줄 존재 확인
        WorkspaceShift shift = workspaceShiftQueryRepository.findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 근무 스케줄입니다."));

        // 과거 스케줄인지 확인
        if (shift.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "과거 스케줄에 대한 교환 요청은 불가능합니다.");
        }

        // 사용자가 해당 워크스페이스의 근무자인지 확인
        if (!workspaceQueryRepository.isUserActiveWorkerInWorkspace(actor.getUser(), shift.getWorkspace().getId())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "해당 업장의 근무자가 아닙니다.");
        }

        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequestDto.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequestDto.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, pageRequestDto.pageSize());

        long totalCount = workspaceQueryRepository.getExchangeableWorkerCount(
            shift.getWorkspace().getId(), 
            actor.getUser(), 
            shift.getStartDateTime(), 
            shift.getEndDateTime()
        );
        if (totalCount == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        List<UserWorkspaceWorkerListResponse> workerList = workspaceQueryRepository.getExchangeableWorkerListWithCursor(
            shift.getWorkspace().getId(), 
            actor.getUser(), 
            shift.getStartDateTime(), 
            shift.getEndDateTime(), 
            pageRequest
        );
        if (ObjectUtils.isEmpty(workerList)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequestDto.pageSize(), (int) totalCount));
        }

        UserWorkspaceWorkerListResponse last = workerList.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) totalCount
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            workerList.stream()
                .map(ExchangeableWorkerResponseDto::of)
                .toList()
        );
    }
}
