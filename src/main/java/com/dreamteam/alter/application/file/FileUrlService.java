package com.dreamteam.alter.application.file;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.PresignedUrlCacheRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.file.type.BucketType;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("fileUrlService")
@RequiredArgsConstructor
public class FileUrlService {

    @Resource(name = "s3Client")
    private final S3Client s3Client;

    private final PresignedUrlCacheRepository presignedUrlCacheRepository;

    public PresignedUrlResult getPresignedUrl(File file) {
        return presignedUrlCacheRepository.findByFileId(file.getId())
            .orElseGet(() -> {
                PresignedUrlResult result = s3Client.getPresignedUrl(file.getStoredKey(), file.getBucketType());
                presignedUrlCacheRepository.save(file.getId(), result);
                return result;
            });
    }

    public FileResponseDto resolve(File file) {
        String url;
        if (BucketType.PUBLIC.equals(file.getBucketType())) {
            url = file.getFileUrl();
        } else {
            url = getPresignedUrl(file).url();
        }

        return FileResponseDto.of(file, url);
    }
}
