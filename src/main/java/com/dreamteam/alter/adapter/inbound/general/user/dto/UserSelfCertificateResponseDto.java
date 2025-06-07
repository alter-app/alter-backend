package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.entity.UserCertificate;
import com.dreamteam.alter.domain.user.type.CertificateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 자격증 응답 DTO")
public class UserSelfCertificateResponseDto {

    @NotNull
    @Schema(description = "등록 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "자격증 종류", example = "CERTIFICATE")
    private CertificateType type;

    @NotBlank
    @Schema(description = "자격 이름", example = "정보처리기사")
    private String certificateName;

    @Nullable
    @Schema(description = "자격증 ID", example = "1234567890")
    private String certificateId;

    @NotBlank
    @Schema(description = "발행기관", example = "한국산업인력공단")
    private String publisherName;

    @NotNull
    @Schema(description = "취득일", example = "2023-10-01")
    private LocalDate issuedAt;

    @Nullable
    @Schema(description = "만료일", example = "2024-10-01")
    private LocalDate expiresAt;

    @NotNull
    @Schema(description = "최종 수정일", example = "2023-10-01T12:00:00")
    private LocalDateTime updatedAt;

    public static UserSelfCertificateResponseDto from(UserCertificate entity) {
        return UserSelfCertificateResponseDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .certificateName(entity.getCertificateName())
                .certificateId(entity.getCertificateId())
                .publisherName(entity.getPublisherName())
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
