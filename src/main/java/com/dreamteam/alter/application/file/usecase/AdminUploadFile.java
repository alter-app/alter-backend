package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.adapter.inbound.admin.file.dto.AdminUploadFileResponseDto;
import com.dreamteam.alter.application.file.FileUploadService;
import com.dreamteam.alter.domain.file.port.inbound.AdminUploadFileUseCase;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AdminActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("adminUploadFile")
@RequiredArgsConstructor
public class AdminUploadFile implements AdminUploadFileUseCase {

    @Resource(name = "fileUploadService")
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public AdminUploadFileResponseDto execute(AdminActor actor, MultipartFile file, FileTargetType targetType, BucketType bucketType) {
        String fileId = fileUploadService.upload(file, targetType, bucketType, actor.getUserId());
        return AdminUploadFileResponseDto.of(fileId);
    }
}
