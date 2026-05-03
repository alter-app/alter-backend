package com.dreamteam.alter.adapter.inbound.manager.user.dto;

import com.dreamteam.alter.domain.user.result.GetUserSelfInfoResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 자신의 정보 응답 DTO")
public class ManagerSelfInfoResponseDto {

    @NotNull
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "매니저 이름", example = "김철수")
    private String name;

    @NotBlank
    @Schema(description = "매니저 닉네임", example = "김땡땡")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://alter-public.s3.ap-northeast-2.amazonaws.com/user_profile/example.png")
    private String profileImageUrl;

    @NotNull
    @Schema(description = "가입일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    public static ManagerSelfInfoResponseDto from(GetUserSelfInfoResult result) {
        return ManagerSelfInfoResponseDto.builder()
            .id(result.id())
            .name(result.name())
            .nickname(result.nickname())
            .profileImageUrl(result.profileImageUrl())
            .createdAt(result.createdAt())
            .build();
    }
}
