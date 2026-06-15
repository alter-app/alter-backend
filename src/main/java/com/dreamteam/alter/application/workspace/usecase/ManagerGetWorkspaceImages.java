package com.dreamteam.alter.application.workspace.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceImageResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.port.inbound.ManagerGetWorkspaceImagesUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("managerGetWorkspaceImages")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetWorkspaceImages implements ManagerGetWorkspaceImagesUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceImageQueryRepository workspaceImageQueryRepository;
    private final FileQueryRepository fileQueryRepository;
    private final FileUrlService fileUrlService;

    @Override
    public List<WorkspaceImageResponseDto> execute(ManagerActor actor, Long workspaceId) {
        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND);
        }

        List<WorkspaceImage> images = workspaceImageQueryRepository.findAllByWorkspaceId(workspaceId);
        if (images.isEmpty()) {
            return List.of();
        }

        List<String> fileIds = images.stream()
            .map(WorkspaceImage::getFileId)
            .toList();
        Map<String, File> fileMap = fileQueryRepository.findAllByIdIn(fileIds).stream()
            .collect(Collectors.toMap(File::getId, Function.identity()));

        List<WorkspaceImageResponseDto> result = new ArrayList<>();
        int sortOrder = 0;
        for (WorkspaceImage image : images) {
            File file = fileMap.get(image.getFileId());
            if (file == null) {
                continue;
            }
            result.add(WorkspaceImageResponseDto.of(file.getId(), fileUrlService.resolve(file).getUrl(), sortOrder++));
        }
        return result;
    }
}
