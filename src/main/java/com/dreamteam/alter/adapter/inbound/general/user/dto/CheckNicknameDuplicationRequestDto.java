package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "닉네임 중복 확인 요청 DTO")
public class CheckNicknameDuplicationRequestDto {

    @Size(max = 64)
    @Schema(description = "닉네임", example = "유땡땡")
    private String nickname;

}
