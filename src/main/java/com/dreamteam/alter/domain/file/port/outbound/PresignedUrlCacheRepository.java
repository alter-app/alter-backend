package com.dreamteam.alter.domain.file.port.outbound;

import com.dreamteam.alter.domain.file.PresignedUrlResult;

import java.util.Optional;

public interface PresignedUrlCacheRepository {
    Optional<PresignedUrlResult> findByFileId(String fileId);
    void save(String fileId, PresignedUrlResult result);
    void deleteByFileId(String fileId);
}
