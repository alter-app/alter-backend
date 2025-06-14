package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.domain.user.entity.UserCertificate;
import com.dreamteam.alter.domain.user.type.CertificateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 관리자 공고 지원자 자격증 정보 DTO")
public class PostingApplicationResponseUserCertificateDto {

    @NotNull
    @Schema(description = "자격증 등록 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "자격증 종류", example = "LICENSE")
    private CertificateType type;

    @NotBlank
    @Schema(description = "자격증 이름", example = "식품위생관리사")
    private String certificateName;

    @NotBlank
    @Schema(description = "자격증 ID", example = "CERT-123456")
    private String certificateId;

    @NotBlank
    @Schema(description = "발급 기관 이름", example = "한국식품안전관리원")
    private String publisherName;

    @NotNull
    @Schema(description = "발급 일자", example = "2023-01-01")
    private LocalDate issuedAt;

    @Nullable
    @Schema(description = "만료 일자", example = "2025-01-01")
    private LocalDate expiresAt;

    public static PostingApplicationResponseUserCertificateDto from(UserCertificate entity) {
        return PostingApplicationResponseUserCertificateDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .certificateName(entity.getCertificateName())
                .certificateId(entity.getCertificateId())
                .publisherName(entity.getPublisherName())
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

}
