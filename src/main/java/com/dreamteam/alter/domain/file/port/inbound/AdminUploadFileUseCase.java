package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.adapter.inbound.admin.file.dto.AdminUploadFileResponseDto;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AdminActor;
import org.springframework.web.multipart.MultipartFile;

public interface AdminUploadFileUseCase {
    AdminUploadFileResponseDto execute(AdminActor actor, MultipartFile file, FileTargetType targetType, BucketType bucketType);
}
