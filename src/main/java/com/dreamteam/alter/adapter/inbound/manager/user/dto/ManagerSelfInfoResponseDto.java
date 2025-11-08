package com.dreamteam.alter.adapter.inbound.manager.user.dto;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.ManagerSelfInfoResponse;
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
    @Schema(description = "매니저 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "매니저 이름", example = "김철수")
    private String name;

    @NotBlank
    @Schema(description = "매니저 닉네임", example = "김땡땡")
    private String nickname;

    @NotNull
    @Schema(description = "가입일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    public static ManagerSelfInfoResponseDto from(ManagerSelfInfoResponse entity) {
        return ManagerSelfInfoResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .nickname(entity.getNickname())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
