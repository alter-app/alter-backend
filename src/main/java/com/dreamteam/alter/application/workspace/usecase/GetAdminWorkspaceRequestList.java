package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.AdminWorkspaceRequestListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service("getAdminWorkspaceRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdminWorkspaceRequestList implements GetAdminWorkspaceRequestListUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final ObjectMapper objectMapper;

	@Override
	public CursorPaginatedApiResponse<AdminWorkspaceRequestListResponseDto> execute(CursorPageRequestDto request) {
		CursorDto cursorDto = null;
		if (ObjectUtils.isNotEmpty(request.cursor())) {
			cursorDto = CursorUtil.decodeCursor(request.cursor(), CursorDto.class, objectMapper);
		}
		CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, request.pageSize());

		long count = workspaceRequestQueryRepository.countAll();
		if (count == 0) {
			return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
		}

		List<AdminWorkspaceRequestListResponse> requests =
			workspaceRequestQueryRepository.getAdminWorkspaceRequestListWithCursor(pageRequest);
		if (ObjectUtils.isEmpty(requests)) {
			return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(request.pageSize(), (int) count));
		}

		AdminWorkspaceRequestListResponse last = requests.getLast();
		CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
			CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
			pageRequest.pageSize(),
			(int) count
		);

		return CursorPaginatedApiResponse.of(
			pageResponseDto,
			requests.stream()
				.map(AdminWorkspaceRequestListResponseDto::from)
				.toList()
		);
	}
}
