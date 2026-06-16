package com.dreamteam.alter.application.workspace.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.UpdateWorkspaceRequestStatusUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;

import lombok.RequiredArgsConstructor;

@Service("updateWorkspaceRequestStatus")
@RequiredArgsConstructor
@Transactional
public class UpdateWorkspaceRequestStatus implements UpdateWorkspaceRequestStatusUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceRepository workspaceRepository;
	private final ManagerUserQueryRepository managerUserQueryRepository;
	private final ManagerUserRepository managerUserRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileDeleteService fileDeleteService;

	@Override
	public void execute(Long workspaceRequestId, WorkspaceRequestStatus status) {
		if (status != WorkspaceRequestStatus.ACTIVATED && status != WorkspaceRequestStatus.REVOKED) {
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "ACTIVATED 또는 REVOKED 만 설정 가능합니다.");
		}

		WorkspaceRequest workspaceRequest = workspaceRequestQueryRepository.findByIdWithUser(workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 업장 등록 신청을 찾을 수 없습니다."));

		if (status == WorkspaceRequestStatus.ACTIVATED) {
			approve(workspaceRequest, workspaceRequestId);
		} else {
			workspaceRequest.reject();
		}
	}

	private void approve(WorkspaceRequest workspaceRequest, Long workspaceRequestId) {
		workspaceRequest.approve();

		ManagerUser managerUser = managerUserQueryRepository.findByUserId(workspaceRequest.getUser().getId())
			.orElseGet(() -> managerUserRepository.save(ManagerUser.create(workspaceRequest.getUser(), ManagerUserStatus.ACTIVATED)));

		Workspace workspace = Workspace.create(
			managerUser,
			workspaceRequest.getBusinessRegistrationNo(),
			workspaceRequest.getBusinessName(),
			workspaceRequest.getBusinessType(),
			workspaceRequest.getContact(),
			null,
			WorkspaceStatus.ACTIVATED,
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
