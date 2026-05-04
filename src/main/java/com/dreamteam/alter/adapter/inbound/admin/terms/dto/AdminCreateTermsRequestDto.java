package com.dreamteam.alter.adapter.inbound.admin.terms.dto;

import com.dreamteam.alter.domain.terms.type.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "약관 생성 요청 DTO")
public class AdminCreateTermsRequestDto {

    @NotNull
    @Schema(description = "약관 유형", example = "SERVICE", implementation = TermsType.class)
    private TermsType type;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^v\\d+\\.\\d+$", message = "버전 형식은 v{숫자}.{숫자} 이어야 합니다")
    @Schema(description = "약관 버전", example = "v1.0")
    private String version;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "약관 제목", example = "서비스 이용약관")
    private String title;

    @NotBlank
    @Size(max = 1000)
    @Pattern(regexp = "^https://.*", message = "https URL이어야 합니다")
    @Schema(description = "노션 URL", example = "https://notion.so/terms")
    private String notionUrl;

    @Schema(description = "필수 동의 여부", example = "true")
    private boolean required;
}
