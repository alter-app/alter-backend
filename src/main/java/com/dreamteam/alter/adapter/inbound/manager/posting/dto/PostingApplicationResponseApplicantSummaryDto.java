package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 관리자 공고 지원자 요약 정보 DTO")
public class PostingApplicationResponseApplicantSummaryDto {

    @NotBlank
    @Schema(description = "이름", example = "홍길동")
    private String name;

    public static PostingApplicationResponseApplicantSummaryDto from(User entity) {
        return PostingApplicationResponseApplicantSummaryDto.builder()
            .name(entity.getName())
            .build();
    }

}
