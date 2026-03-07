package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AdminDeleteFileUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.user.context.AdminActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminDeleteFile")
@RequiredArgsConstructor
public class AdminDeleteFile implements AdminDeleteFileUseCase {

    private final FileQueryRepository fileQueryRepository;

    @Resource(name = "s3Client")
    private final S3Client s3Client;

    @Override
    @Transactional
    public void execute(AdminActor actor, String fileId) {
        File file = fileQueryRepository.findById(fileId)
            .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        s3Client.delete(file.getStoredKey(), file.getBucketType());
        file.markDeleted();
    }
}
