package com.dreamteam.alter.adapter.inbound.general.terms.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.terms.result.GetPublishedTermsListResult;
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
@Schema(description = "게시된 약관 항목 응답 DTO")
public class PublishedTermsItemResponseDto {

    @Schema(description = "약관 ID", example = "1")
    private Long id;

    @Schema(description = "약관 유형")
    private DescribedEnumDto<TermsType> type;

    @Schema(description = "약관 버전", example = "v1.0")
    private String version;

    @Schema(description = "약관 제목", example = "서비스 이용약관")
    private String title;

    @Schema(description = "약관 문서 URL", example = "https://example.com/terms/service/v1.0")
    private String docUrl;

    @Schema(description = "필수 동의 여부", example = "true")
    private boolean required;

    @Schema(description = "게시 일시", example = "2025-01-01T12:00:00")
    private LocalDateTime effectiveAt;

    public static PublishedTermsItemResponseDto from(GetPublishedTermsListResult result) {
        return PublishedTermsItemResponseDto.builder()
                .id(result.id())
                .type(DescribedEnumDto.of(result.type(), TermsType.describe()))
                .version("v" + result.version())
                .title(result.title())
                .docUrl(result.docUrl())
                .required(result.required())
                .effectiveAt(result.effectiveAt())
                .build();
    }
}
