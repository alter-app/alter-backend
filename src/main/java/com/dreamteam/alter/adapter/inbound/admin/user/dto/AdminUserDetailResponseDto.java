package com.dreamteam.alter.adapter.inbound.admin.user.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.AdminUserDetailResponse;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.dreamteam.alter.domain.user.type.UserStatus;
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
@Schema(description = "회원 상세 응답 DTO")
public class AdminUserDetailResponseDto {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "닉네임", example = "알터유저")
    private String nickname;

    @Schema(description = "연락처", example = "010-1234-5678")
    private String contact;

    @Schema(description = "생년월일", example = "19900101")
    private String birthday;

    @Schema(description = "성별")
    private DescribedEnumDto<UserGender> gender;

    @Schema(description = "역할")
    private DescribedEnumDto<UserRole> role;

    @Schema(description = "상태")
    private DescribedEnumDto<UserStatus> status;

    @Schema(description = "생성일시", example = "2025-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-01-01T12:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "평판 요약")
    private AdminUserReputationSummaryDto reputationSummary;

    public static AdminUserDetailResponseDto from(AdminUserDetailResponse response) {
        return AdminUserDetailResponseDto.builder()
            .id(response.getId())
            .email(response.getEmail())
            .name(response.getName())
            .nickname(response.getNickname())
            .contact(response.getContact())
            .birthday(response.getBirthday())
            .gender(DescribedEnumDto.of(response.getGender(), UserGender.describe()))
            .role(DescribedEnumDto.of(response.getRole(), UserRole.describe()))
            .status(DescribedEnumDto.of(response.getStatus(), UserStatus.describe()))
            .createdAt(response.getCreatedAt())
            .updatedAt(response.getUpdatedAt())
            .reputationSummary(AdminUserReputationSummaryDto.from(response.getReputationSummary()))
            .build();
    }
}
