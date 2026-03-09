package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.file.PresignedUrlResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "Presigned URL 조회 응답 DTO")
public class FilePresignedUrlResponseDto {

    @Schema(description = "S3 Presigned URL (유효시간 제한 있음)")
    private String presignedUrl;

    @Schema(description = "Presigned URL 만료 시각 (epoch milliseconds)")
    private long expiresAt;

    public static FilePresignedUrlResponseDto of(PresignedUrlResult result) {
        return new FilePresignedUrlResponseDto(result.url(), result.expiresAt().toEpochMilli());
    }
}
