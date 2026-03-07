package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.adapter.inbound.admin.file.dto.AdminGetPresignedUrlResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AdminGetPresignedUrlUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.user.context.AdminActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminGetPresignedUrl")
@RequiredArgsConstructor
public class AdminGetPresignedUrl implements AdminGetPresignedUrlUseCase {

    private final FileQueryRepository fileQueryRepository;

    @Resource(name = "s3Client")
    private final S3Client s3Client;

    @Override
    @Transactional(readOnly = true)
    public AdminGetPresignedUrlResponseDto execute(AdminActor actor, String fileId) {
        File file = fileQueryRepository.findById(fileId)
            .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        String presignedUrl = s3Client.getPresignedUrl(file.getStoredKey(), file.getBucketType());
        return AdminGetPresignedUrlResponseDto.of(presignedUrl);
    }
}
