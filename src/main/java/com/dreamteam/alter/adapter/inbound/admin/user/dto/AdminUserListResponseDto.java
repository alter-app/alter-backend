package com.dreamteam.alter.adapter.inbound.admin.user.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.AdminUserListResponse;
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
@Schema(description = "회원 목록 응답 DTO")
public class AdminUserListResponseDto {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "닉네임", example = "알터유저")
    private String nickname;

    @Schema(description = "역할")
    private DescribedEnumDto<UserRole> role;

    @Schema(description = "상태")
    private DescribedEnumDto<UserStatus> status;

    @Schema(description = "가입일시", example = "2025-01-01T12:00:00")
    private LocalDateTime createdAt;

    public static AdminUserListResponseDto from(AdminUserListResponse response) {
        return AdminUserListResponseDto.builder()
            .id(response.getId())
            .email(response.getEmail())
            .name(response.getName())
            .nickname(response.getNickname())
            .role(DescribedEnumDto.of(response.getRole(), UserRole.describe()))
            .status(DescribedEnumDto.of(response.getStatus(), UserStatus.describe()))
            .createdAt(response.getCreatedAt())
            .build();
    }
}
