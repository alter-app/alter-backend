package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SendWorkspaceInvitationRequestDto {

    @NotEmpty
    private List<@NotBlank @Size(min = 10, max = 11) String> phoneNumbers;
}
