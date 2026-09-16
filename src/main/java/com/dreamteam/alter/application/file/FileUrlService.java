package com.dreamteam.alter.application.file;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.port.outbound.PresignedUrlCacheRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("fileUrlService")
@RequiredArgsConstructor
public class FileUrlService {

    @Resource(name = "s3Client")
    private final S3Client s3Client;

    private final PresignedUrlCacheRepository presignedUrlCacheRepository;

    private final FileQueryRepository fileQueryRepository;

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

    public String resolveUrlByTarget(FileTargetType targetType, String targetId) {
        return fileQueryRepository.findByTargetTypeAndTargetId(targetType, targetId)
            .map(file -> resolve(file).getUrl())
            .orElse(null);
    }

    // 대상 id별로 ATTACHED 파일 중 최신 1건(그룹의 마지막 원소, ADR-014)을 골라 URL로 변환한다.
    // resolve 호출은 대상 수만큼만 발생한다(대상별 파일 개수와 무관).
    public Map<String, String> resolveLatestUrlsByTarget(FileTargetType targetType, List<String> targetIds) {
        return fileQueryRepository.findAllByTargetTypeAndTargetIdIn(targetType, targetIds)
            .stream()
            .collect(Collectors.groupingBy(File::getTargetId))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> resolve(entry.getValue().stream().reduce((first, second) -> second).orElseThrow()).getUrl()
            ));
    }

    public String resolveLatestUrlByTarget(FileTargetType targetType, String targetId) {
        return resolveLatestUrlsByTarget(targetType, List.of(targetId)).get(targetId);
    }
}
