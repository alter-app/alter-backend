package com.dreamteam.alter.adapter.inbound.general.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "파일 업로드 응답 DTO")
public class AppUploadFileResponseDto {

    @Schema(description = "파일 ID", example = "01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e")
    private String fileId;

    public static AppUploadFileResponseDto of(String fileId) {
        return new AppUploadFileResponseDto(fileId);
    }
}
