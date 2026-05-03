package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "닉네임 변경 요청 DTO")
public class UpdateNicknameRequestDto {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "변경할 닉네임 (최대 64자)", example = "newNickname")
    private String nickname;
}
