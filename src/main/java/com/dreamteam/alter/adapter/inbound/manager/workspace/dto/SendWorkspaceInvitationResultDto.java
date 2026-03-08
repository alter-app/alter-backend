package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "직원 초대 발송 결과 DTO")
public class SendWorkspaceInvitationResultDto {

    @Schema(description = "초대 성공 인원 수", example = "3")
    private int successCount;

    @Schema(description = "앱 미가입 휴대폰 번호 목록", example = "[\"01011112222\"]")
    private List<String> unregisteredPhoneNumbers;

    @Schema(description = "이미 해당 업장에 근무 중인 사용자의 휴대폰 번호 목록", example = "[\"01033334444\"]")
    private List<String> alreadyWorkerPhoneNumbers;

    @Schema(description = "이미 초대가 진행 중인 사용자의 휴대폰 번호 목록", example = "[\"01055556666\"]")
    private List<String> alreadyInvitedPhoneNumbers;
}
