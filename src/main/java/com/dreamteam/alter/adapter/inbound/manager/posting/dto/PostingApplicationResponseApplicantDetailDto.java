package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 관리자 공고 지원자 상세 정보 DTO")
public class PostingApplicationResponseApplicantDetailDto {

    @NotNull
    @Schema(description = "공고 지원 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @NotBlank
    @Schema(description = "이메일", example = "example@test.com")
    private String email;

    @NotBlank
    @Schema(description = "연락처", example = "010-1234-5678")
    private String contact;

    @NotBlank
    @Schema(description = "생년월일", example = "2000-01-01")
    private String birthday;

    @NotNull
    @Schema(description = "성별", example = "GENDER_MALE")
    private UserGender gender;

    @Schema(description = "지원자 자격증 목록")
    List<PostingApplicationResponseUserCertificateDto> userCertificates;

    @Schema(description = "지원자 평판 요약 정보")
    private ReputationSummaryDto reputationSummary;

    public static PostingApplicationResponseApplicantDetailDto from(User entity) {
        return PostingApplicationResponseApplicantDetailDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .email(entity.getEmail())
            .contact(entity.getContact())
            .birthday(entity.getBirthday())
            .gender(entity.getGender())
            .userCertificates(entity.getCertificates()
                .stream()
                .map(PostingApplicationResponseUserCertificateDto::from)
                .toList())
            .build();
    }

    public static PostingApplicationResponseApplicantDetailDto from(User entity, ReputationSummaryDto reputationSummary) {
        return PostingApplicationResponseApplicantDetailDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .email(entity.getEmail())
            .contact(entity.getContact())
            .birthday(entity.getBirthday())
            .gender(entity.getGender())
            .userCertificates(entity.getCertificates()
                .stream()
                .map(PostingApplicationResponseUserCertificateDto::from)
                .toList())
            .reputationSummary(reputationSummary)
            .build();
    }

}
