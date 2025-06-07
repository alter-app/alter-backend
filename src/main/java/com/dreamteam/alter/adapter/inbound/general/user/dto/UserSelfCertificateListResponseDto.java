package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.user.entity.UserCertificate;
import com.dreamteam.alter.domain.user.type.CertificateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 자격 및 면허증 목록 응답 DTO")
public class UserSelfCertificateListResponseDto {

    @NotNull
    @Schema(description = "등록 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "자격증 종류", example = "CERTIFICATE")
    private CertificateType type;

    @NotBlank
    @Schema(description = "자격 이름", example = "정보처리기사")
    private String certificateName;

    @NotBlank
    @Schema(description = "발행기관", example = "한국산업인력공단")
    private String publisherName;

    @NotNull
    @Schema(description = "취득일", example = "2023-10-01")
    private LocalDate issuedAt;

    public static UserSelfCertificateListResponseDto from(UserCertificate entity) {
        return UserSelfCertificateListResponseDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .certificateName(entity.getCertificateName())
                .publisherName(entity.getPublisherName())
                .issuedAt(entity.getIssuedAt())
                .build();
    }

}
