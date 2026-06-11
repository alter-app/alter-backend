package com.dreamteam.alter.application.workspace.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.ApproveWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;

import lombok.RequiredArgsConstructor;

@Service("approveWorkspaceRequest")
@RequiredArgsConstructor
@Transactional
public class ApproveWorkspaceRequest implements ApproveWorkspaceRequestUseCase {

	private final WorkspaceRepository workspaceRepository;
	private final ManagerUserQueryRepository managerUserQueryRepository;
	private final ManagerUserRepository managerUserRepository;
	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileDeleteService fileDeleteService;
	private final AuthService authService;

	@Override
	public void execute(Long workspaceRequestId) {
		WorkspaceRequest workspaceRequest = workspaceRequestQueryRepository.findByIdWithUser(workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 업장 등록 요청을 찾을 수 없습니다."));

		workspaceRequest.approve();

		User requester = workspaceRequest.getUser();

		// USER -> MANAGER 승급. 실제로 승급된 경우에만 기존 인증 세션을 무효화한다.
		if (requester.promoteToManager()) {
			authService.revokeAllExistingAuthorizations(requester);
		}

		ManagerUser managerUser = managerUserQueryRepository.findByUserId(requester.getId())
			.orElseGet(() -> managerUserRepository.save(ManagerUser.create(requester, ManagerUserStatus.ACTIVATED)));

		Workspace workspace = Workspace.create(
			managerUser,
			workspaceRequest.getBusinessRegistrationNo(),
			workspaceRequest.getBusinessName(),
			workspaceRequest.getBusinessType(),
			workspaceRequest.getContact(),
			null,
			WorkspaceStatus.ACTIVATED,
			workspaceRequest.getOwnerName(),
			workspaceRequest.getFullAddress(),
			workspaceRequest.getProvince(),
			workspaceRequest.getDistrict(),
			workspaceRequest.getTown(),
			workspaceRequest.getLatitude(),
			workspaceRequest.getLongitude()
		);

		workspaceRepository.save(workspace);

		Optional<File> file = fileQueryRepository.findByTargetTypeAndTargetId(
			FileTargetType.WORKSPACE_OWN_IDENTITY, String.valueOf(workspaceRequestId));

		file.ifPresent(fileDeleteService::delete);
	}
}
