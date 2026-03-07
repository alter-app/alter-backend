package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerUploadFileResponseDto;
import com.dreamteam.alter.application.file.FileUploadService;
import com.dreamteam.alter.domain.file.port.inbound.ManagerUploadFileUseCase;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("managerUploadFile")
@RequiredArgsConstructor
public class ManagerUploadFile implements ManagerUploadFileUseCase {

    @Resource(name = "fileUploadService")
    private final FileUploadService fileUploadService;

    @Override
    public ManagerUploadFileResponseDto execute(ManagerActor actor, MultipartFile file, FileTargetType targetType, BucketType bucketType) {
        String fileId = fileUploadService.upload(file, targetType, bucketType, actor.getUserId());
        return ManagerUploadFileResponseDto.of(fileId);
    }
}
