package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListResponseDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportListResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.report.port.inbound.ManagerGetMyReportListUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("managerGetMyReportList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetMyReportList implements ManagerGetMyReportListUseCase {

    private final ReportQueryRepository reportQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ReportListResponseDto> execute(
        CursorPageRequestDto request,
        ReportListFilterDto filter,
        ManagerActor actor
    ) {
        // 매니저는 workspace 필터를 사용할 수 없음
        if (ReportTargetType.WORKSPACE.equals(filter.getTargetType())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "매니저는 업장 신고를 조회할 수 없습니다.");
        }
        
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = reportQueryRepository.getCountOfReports(
            filter,
            ReporterType.MANAGER,
            actor.getManagerUser().getId()
        );
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        List<ReportListResponse> reports = reportQueryRepository.getReportsWithCursor(
            pageRequest,
            filter,
            ReporterType.MANAGER,
            actor.getManagerUser().getId()
        );
        if (ObjectUtils.isEmpty(reports)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        ReportListResponse last = reports.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            reports.stream()
                .map(ReportListResponseDto::from)
                .toList()
        );
    }
}
