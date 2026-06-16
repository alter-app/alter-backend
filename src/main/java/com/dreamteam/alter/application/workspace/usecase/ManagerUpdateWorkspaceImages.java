package com.dreamteam.alter.application.workspace.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceImagesRequestDto;
import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerUpdateWorkspaceImagesUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("managerUpdateWorkspaceImages")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdateWorkspaceImages implements ManagerUpdateWorkspaceImagesUseCase {

    private static final int MAX_IMAGE_COUNT = 5;

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceImageQueryRepository workspaceImageQueryRepository;
    private final WorkspaceImageRepository workspaceImageRepository;
    private final FileQueryRepository fileQueryRepository;
    private final FileDeleteService fileDeleteService;
    private final AttachFilesUseCase attachFiles;

    @Override
    public void execute(ManagerActor actor, Long workspaceId, UpdateWorkspaceImagesRequestDto request) {
        List<String> newFileIds = CollectionUtils.isEmpty(request.getFileIds())
            ? List.of()
            : request.getFileIds().stream().distinct().toList();

        if (newFileIds.size() > MAX_IMAGE_COUNT) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "대표이미지는 최대 " + MAX_IMAGE_COUNT + "개까지 등록할 수 있습니다.");
        }

        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));
        if (!Objects.equals(workspace.getManagerUser().getId(), actor.getManagerUser().getId())) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        List<WorkspaceImage> existingImages = workspaceImageQueryRepository.findAllByWorkspaceId(workspaceId);
        Map<String, WorkspaceImage> existingByFileId = existingImages.stream()
            .collect(Collectors.toMap(WorkspaceImage::getFileId, Function.identity()));

        // 1. 신규 파일 attach (requestId 단계 없이 바로 workspaceId 로 연결)
        //    검증 실패(FILE_NOT_FOUND 등)를 S3 물리 삭제 이전에 조기에 걸러내 정합성 보장
        List<String> addFileIds = newFileIds.stream()
            .filter(fileId -> !existingByFileId.containsKey(fileId))
            .toList();
        if (!addFileIds.isEmpty()) {
            attachFiles.execute(
                addFileIds,
                FileTargetType.WORKSPACE_REPRESENTATIVE_IMAGE,
                String.valueOf(workspaceId),
                actor.getUserId()
            );
        }

        // 2. 순서 재계산: 유지 이미지는 sortOrder 갱신, 신규는 생성
        List<WorkspaceImage> newImages = new ArrayList<>();
        for (int sortOrder = 0; sortOrder < newFileIds.size(); sortOrder++) {
            String fileId = newFileIds.get(sortOrder);
            WorkspaceImage existing = existingByFileId.get(fileId);
            if (existing != null) {
                existing.updateSortOrder(sortOrder);
            } else {
                newImages.add(WorkspaceImage.create(workspace, fileId, sortOrder));
            }
        }
        if (!newImages.isEmpty()) {
            workspaceImageRepository.saveAll(newImages);
        }

        // 3. 빠진 이미지 제거 (파일 soft-delete + WorkspaceImage 삭제)
        //    S3 물리 삭제는 롤백 불가하므로 attach/검증이 모두 성공한 마지막 단계에서 수행
        List<WorkspaceImage> removedImages = existingImages.stream()
            .filter(image -> !newFileIds.contains(image.getFileId()))
            .toList();
        if (!removedImages.isEmpty()) {
            List<String> removedFileIds = removedImages.stream()
                .map(WorkspaceImage::getFileId)
                .toList();
            fileQueryRepository.findAllByIdIn(removedFileIds).forEach(fileDeleteService::delete);
            workspaceImageRepository.deleteAll(removedImages);
        }
    }
}
