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
@Schema(description = "이메일 찾기 요청 DTO")
public class FindEmailRequestDto {

    @NotBlank
    @Size(max = 13)
    @Schema(description = "전화번호 (하이픈 미포함)")
    private String contact;
}
