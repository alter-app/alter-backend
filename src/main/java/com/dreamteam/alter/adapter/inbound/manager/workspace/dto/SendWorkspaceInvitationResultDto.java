package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SendWorkspaceInvitationResultDto {

    private int successCount;
    private List<String> unregisteredPhoneNumbers;
    private List<String> alreadyWorkerPhoneNumbers;
    private List<String> alreadyInvitedPhoneNumbers;
}
