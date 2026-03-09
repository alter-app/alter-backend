package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerUploadFileResponseDto;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import org.springframework.web.multipart.MultipartFile;

public interface ManagerUploadFileUseCase {
    ManagerUploadFileResponseDto execute(ManagerActor actor, MultipartFile file, FileTargetType targetType, BucketType bucketType);
}
