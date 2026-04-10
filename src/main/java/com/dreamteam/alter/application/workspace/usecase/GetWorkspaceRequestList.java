package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getWorkspaceRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceRequestList implements GetWorkspaceRequestListUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

	@Override
	public List<WorkspaceRequestListResponseDto> execute(AppActor actor) {
		return workspaceRequestQueryRepository.getWorkspaceRequestList(actor.getUserId()).stream()
			.map(WorkspaceRequestListResponseDto::of)
			.toList();
	}
}
