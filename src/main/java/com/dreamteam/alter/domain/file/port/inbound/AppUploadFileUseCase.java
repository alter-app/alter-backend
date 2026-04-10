package com.dreamteam.alter.domain.file.port.inbound;

import org.springframework.web.multipart.MultipartFile;

import com.dreamteam.alter.adapter.inbound.general.file.dto.AppUploadFileResponseDto;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface AppUploadFileUseCase {
    AppUploadFileResponseDto execute(AppActor actor, MultipartFile file, FileTargetType targetType, BucketType bucketType);
}
