package com.dreamteam.alter.application.report.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListResponseDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.report.port.inbound.GetMyReportListUseCase;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getMyReportList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyReportList implements GetMyReportListUseCase {

    private final ReportQueryRepository reportQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<ReportListResponseDto> execute(
        CursorPageRequestDto request,
        ReportListFilterDto filter,
        AppActor actor
    ) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(request.cursor())) {
            cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

        long count = reportQueryRepository.getCountOfReports(filter, ReporterType.USER, actor.getUser().getId());
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
        }

        List<ReportListResponse> reports =
            reportQueryRepository.getReportsWithCursor(pageRequest, filter, ReporterType.USER, actor.getUser().getId());
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
