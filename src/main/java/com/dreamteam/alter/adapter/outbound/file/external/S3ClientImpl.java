package com.dreamteam.alter.adapter.outbound.file.external;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.file.type.BucketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component("s3Client")
@RequiredArgsConstructor
public class S3ClientImpl implements S3Client {

    private final software.amazon.awssdk.services.s3.S3Client awsS3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.public-bucket}")
    private String publicBucket;

    @Value("${aws.s3.private-bucket}")
    private String privateBucket;

    @Value("${aws.s3.presigned-url-expiration-minutes:30}")
    private long presignedUrlExpirationMinutes;

    @Override
    public String upload(MultipartFile file, String storedKey, BucketType bucketType) {
        String bucket = resolveBucket(bucketType);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storedKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            awsS3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            if (bucketType == BucketType.PUBLIC) {
                return "https://" + bucket + ".s3.amazonaws.com/" + storedKey;
            }
            return null;
        } catch (IOException e) {
            log.error("S3 upload failed for key={}", storedKey, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public PresignedUrlResult getPresignedUrl(String storedKey, BucketType bucketType) {
        String bucket = resolveBucket(bucketType);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(presignedUrlExpirationMinutes));
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(presignedUrlExpirationMinutes))
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(bucket)
                .key(storedKey)
                .build())
            .build();

        String url = s3Presigner.presignGetObject(presignRequest).url().toString();
        return new PresignedUrlResult(url, expiresAt);
    }

    @Override
    public void delete(String storedKey, BucketType bucketType) {
        String bucket = resolveBucket(bucketType);
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(storedKey)
                .build();
            awsS3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("S3 delete failed for key={}", storedKey, e);
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private String resolveBucket(BucketType bucketType) {
        return bucketType == BucketType.PUBLIC ? publicBucket : privateBucket;
    }
}
