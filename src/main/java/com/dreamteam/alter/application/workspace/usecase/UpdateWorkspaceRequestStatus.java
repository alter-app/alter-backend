package com.dreamteam.alter.application.workspace.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.inbound.UpdateWorkspaceRequestStatusUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("updateWorkspaceRequestStatus")
@RequiredArgsConstructor
@Transactional
public class UpdateWorkspaceRequestStatus implements UpdateWorkspaceRequestStatusUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceRepository workspaceRepository;
	private final ManagerUserQueryRepository managerUserQueryRepository;
	private final ManagerUserRepository managerUserRepository;
	private final WorkspaceImageRepository workspaceImageRepository;
	private final WorkspaceRequestImageQueryRepository workspaceRequestImageQueryRepository;
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

		attachRepresentativeImages(workspaceRequestId, workspace);

		Optional<File> file = fileQueryRepository.findByTargetTypeAndTargetId(
			FileTargetType.WORKSPACE_OWN_IDENTITY, String.valueOf(workspaceRequestId));

		file.ifPresent(fileDeleteService::delete);
	}

	private void attachRepresentativeImages(Long workspaceRequestId, Workspace workspace) {
		List<WorkspaceRequestImage> requestImages =
			workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(workspaceRequestId);

		if (requestImages.isEmpty()) {
			return;
		}

		List<String> fileIds = requestImages.stream()
			.map(WorkspaceRequestImage::getFileId)
			.toList();
		Map<String, File> fileMap = fileQueryRepository.findAllByIdIn(fileIds).stream()
			.collect(Collectors.toMap(File::getId, Function.identity()));

		List<WorkspaceImage> workspaceImages = new ArrayList<>();
		int sortOrder = 0;
		for (WorkspaceRequestImage requestImage : requestImages) {
			File file = fileMap.get(requestImage.getFileId());
			if (file == null) {
				// 신청~승인 사이에 파일이 삭제된 경우. 일부만 마이그레이션되므로 운영 추적용 경고
				log.warn("업장 대표이미지 마이그레이션 중 파일 누락. workspaceRequestId={}, workspaceId={}, fileId={}",
					workspaceRequestId, workspace.getId(), requestImage.getFileId());
				continue;
			}
			// 신청(requestId)에 붙어있던 파일을 생성된 업장(workspaceId)으로 재첨부
			file.attach(String.valueOf(workspace.getId()));
			// 누락 파일이 있어도 노출 순서가 비연속되지 않도록 재계산
			workspaceImages.add(WorkspaceImage.create(workspace, requestImage.getFileId(), sortOrder++));
		}
		workspaceImageRepository.saveAll(workspaceImages);
	}
}
