package com.dreamteam.alter.adapter.inbound.manager.file.dto;

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
public class ManagerGetPresignedUrlResponseDto {

    @Schema(description = "S3 Presigned URL (유효시간 제한 있음)")
    private String presignedUrl;

    @Schema(description = "Presigned URL 만료 시각 (ISO 8601 UTC)")
    private String expiresAt;

    public static ManagerGetPresignedUrlResponseDto of(PresignedUrlResult result) {
        return new ManagerGetPresignedUrlResponseDto(result.url(), result.expiresAt().toString());
    }
}
