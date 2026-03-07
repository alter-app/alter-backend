package com.dreamteam.alter.domain.file.port.outbound;

import com.dreamteam.alter.domain.file.type.BucketType;
import org.springframework.web.multipart.MultipartFile;

public interface S3Client {
    String upload(MultipartFile file, String storedKey, BucketType bucketType);
    String getPresignedUrl(String storedKey, BucketType bucketType);
    void delete(String storedKey, BucketType bucketType);
}
