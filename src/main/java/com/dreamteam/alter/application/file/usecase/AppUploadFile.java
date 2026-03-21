package com.dreamteam.alter.application.file.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dreamteam.alter.adapter.inbound.general.file.dto.AppUploadFileResponseDto;
import com.dreamteam.alter.application.file.FileUploadService;
import com.dreamteam.alter.domain.file.port.inbound.AppUploadFileUseCase;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

@Service("appUploadFile")
@RequiredArgsConstructor
public class AppUploadFile implements AppUploadFileUseCase {

    @Resource(name = "fileUploadService")
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public AppUploadFileResponseDto execute(AppActor actor, MultipartFile file, FileTargetType targetType, BucketType bucketType) {
        String fileId = fileUploadService.upload(file, targetType, bucketType, actor.getUserId());
        return AppUploadFileResponseDto.of(fileId);
    }
}
