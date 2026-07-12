package com.dreamteam.alter.application.workspace.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service("createWorkspaceRequest")
@RequiredArgsConstructor
@Transactional
public class CreateWorkspaceRequest implements CreateWorkspaceRequestUseCase {

	private static final int MAX_IMAGE_COUNT = 5;

	private final WorkspaceRequestRepository workspaceRequestRepository;
	private final WorkspaceRequestImageRepository workspaceRequestImageRepository;
	private final BusinessTypeRepository businessTypeRepository;
	private final AttachFilesUseCase attachFiles;

	@Override
	public void execute(User user, CreateWorkspaceRequestDto request) {
		BusinessType businessType = businessTypeRepository.findById(request.getBusinessTypeId())
			.orElseThrow(() -> new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "존재하지 않는 업종입니다."));
		String businessTypeDetail = resolveBusinessTypeDetail(businessType, request.getBusinessTypeDetail());

		WorkspaceRequest workspaceRequest = WorkspaceRequest.create(
			user,
			request.getBrn(),
			request.getBizName(),
			businessType,
			businessTypeDetail,
			request.getContact(),
			request.getAddress(),
			request.getProvince(),
			request.getDistrict(),
			request.getTown(),
			request.getLatitude(),
			request.getLongitude()
		);

		Long savedWorkspaceRequestId = workspaceRequestRepository.save(workspaceRequest);

		Map<String, FileTargetType> fileMap = new HashMap<>();
		fileMap.put(request.getWorkspaceCertFileId(), FileTargetType.WORKSPACE_CERTIFICATE);
		fileMap.put(request.getWorkspaceOwnIdentityFileId(), FileTargetType.WORKSPACE_OWN_IDENTITY);
		if (request.getWorkspaceWarrantFileId() != null) {
			fileMap.put(request.getWorkspaceWarrantFileId(), FileTargetType.WORKSPACE_WARRANT);
		}
		attachFiles.executeMap(fileMap, savedWorkspaceRequestId.toString(), user.getId());

		List<String> representativeImageFileIds = request.getOrderedRepresentativeImageFileIds();
		if (representativeImageFileIds.size() > MAX_IMAGE_COUNT) {
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "대표이미지는 최대 " + MAX_IMAGE_COUNT + "개까지 등록할 수 있습니다.");
		}
		if (!representativeImageFileIds.isEmpty()) {
			attachFiles.execute(
				representativeImageFileIds,
				FileTargetType.WORKSPACE_REPRESENTATIVE_IMAGE,
				savedWorkspaceRequestId.toString(),
				user.getId()
			);

			List<WorkspaceRequestImage> images = new ArrayList<>();
			for (int sortOrder = 0; sortOrder < representativeImageFileIds.size(); sortOrder++) {
				images.add(WorkspaceRequestImage.create(workspaceRequest, representativeImageFileIds.get(sortOrder), sortOrder));
			}
			workspaceRequestImageRepository.saveAll(images);
		}
	}

	// '기타' 업종만 상세 입력을 요구·저장하고, 그 외 업종의 상세 입력은 무시한다.
	private String resolveBusinessTypeDetail(BusinessType businessType, String rawDetail) {
		if (!businessType.isRequiresDetail()) {
			return null;
		}
		if (StringUtils.isBlank(rawDetail)) {
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "'기타' 업종은 상세 입력이 필요합니다.");
		}
		return StringUtils.trim(rawDetail);
	}
}
