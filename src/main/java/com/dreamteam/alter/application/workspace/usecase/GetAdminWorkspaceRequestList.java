package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestListResponse;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getAdminWorkspaceRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdminWorkspaceRequestList implements GetAdminWorkspaceRequestListUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

	@Override
	public PaginatedResponseDto<AdminWorkspaceRequestListResponseDto> execute(PageRequestDto request) {
		long count = workspaceRequestQueryRepository.countAll();
		PageResponseDto pageResponseDto = PageResponseDto.of(request, (int) count);

		if (count == 0) {
			return PaginatedResponseDto.empty(pageResponseDto);
		}

		List<WorkspaceRequestListResponse> requests =
			workspaceRequestQueryRepository.getWorkspaceRequestListWithOffset(request);

		return PaginatedResponseDto.of(
			pageResponseDto,
			requests.stream()
				.map(AdminWorkspaceRequestListResponseDto::from)
				.toList()
		);
	}
}
