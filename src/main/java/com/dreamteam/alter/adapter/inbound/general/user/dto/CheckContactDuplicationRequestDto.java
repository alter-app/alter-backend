package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "휴대폰 번호 중복 확인 요청 DTO")
public class CheckContactDuplicationRequestDto {

    @Size(min = 10, max = 11)
    @Schema(description = "휴대폰 번호", example = "01012345678")
    private String contact;

}
