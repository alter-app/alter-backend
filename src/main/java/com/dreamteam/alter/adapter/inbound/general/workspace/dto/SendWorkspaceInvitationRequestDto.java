package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "직원 초대 요청 DTO")
public class SendWorkspaceInvitationRequestDto {

    @NotEmpty
    @Schema(description = "초대할 직원의 휴대폰 번호 목록", example = "[\"01012345678\", \"01087654321\"]")
    private List<@NotBlank @Size(min = 10, max = 11) String> phoneNumbers;
}
