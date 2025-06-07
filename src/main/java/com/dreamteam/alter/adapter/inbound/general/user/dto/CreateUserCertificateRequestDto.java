package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.type.CertificateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 자격 정보 생성 요청 DTO")
public class CreateUserCertificateRequestDto {

    @NotNull
    @Schema(description = "자격증 종류", example = "CERTIFICATE")
    private CertificateType type;

    @NotBlank
    @Schema(description = "자격증 이름", example = "정보처리기사")
    private String certificateName;

    @Nullable
    @Schema(description = "자격증 일련번호", example = "1234567890")
    private String certificateId;

    @NotBlank
    @Schema(description = "발행 기관 이름", example = "한국산업인력공단")
    private String publisherName;

    @NotNull
    @Schema(description = "취득일", example = "2023-10-01")
    private LocalDate issuedAt;

    @Nullable
    @Schema(description = "만료일", example = "2025-10-01")
    private LocalDate expiresAt;

}
