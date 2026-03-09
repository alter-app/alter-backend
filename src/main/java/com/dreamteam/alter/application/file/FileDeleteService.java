package com.dreamteam.alter.application.file;

import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.PresignedUrlCacheRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("fileDeleteService")
@RequiredArgsConstructor
public class FileDeleteService {

    @Resource(name = "s3Client")
    private final S3Client s3Client;

    private final PresignedUrlCacheRepository presignedUrlCacheRepository;

    public void delete(File file) {
        s3Client.delete(file.getStoredKey(), file.getBucketType());
        file.markDeleted();
        presignedUrlCacheRepository.deleteByFileId(file.getId());
    }
}
