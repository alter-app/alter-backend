package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SendWorkspaceInvitationRequestDto {

    @NotEmpty
    private List<String> phoneNumbers;
}
