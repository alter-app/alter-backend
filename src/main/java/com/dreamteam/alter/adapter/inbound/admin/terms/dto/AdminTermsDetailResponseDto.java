package com.dreamteam.alter.adapter.inbound.admin.terms.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "약관 상세 응답 DTO")
public class AdminTermsDetailResponseDto {

    @Schema(description = "약관 ID", example = "1")
    private Long id;

    @Schema(description = "약관 유형")
    private DescribedEnumDto<TermsType> type;

    @Schema(description = "약관 버전", example = "v1.0")
    private String version;

    @Schema(description = "약관 제목", example = "서비스 이용약관")
    private String title;

    @Schema(description = "약관 문서 URL", example = "https://notion.so/terms")
    private String docUrl;

    @Schema(description = "필수 동의 여부", example = "true")
    private boolean required;

    @Schema(description = "약관 상태")
    private DescribedEnumDto<TermsStatus> status;

    @Schema(description = "게시 일시", example = "2025-01-01T12:00:00")
    private LocalDateTime effectiveAt;

    @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-01-02T10:00:00")
    private LocalDateTime updatedAt;

    public static AdminTermsDetailResponseDto from(Terms terms) {
        return AdminTermsDetailResponseDto.builder()
                .id(terms.getId())
                .type(DescribedEnumDto.of(terms.getType(), TermsType.describe()))
                .version("v" + terms.getVersion())
                .title(terms.getTitle())
                .docUrl(terms.getDocUrl())
                .required(terms.isRequired())
                .status(DescribedEnumDto.of(terms.getStatus(), TermsStatus.describe()))
                .effectiveAt(terms.getEffectiveAt())
                .createdAt(terms.getCreatedAt())
                .updatedAt(terms.getUpdatedAt())
                .build();
    }
}
