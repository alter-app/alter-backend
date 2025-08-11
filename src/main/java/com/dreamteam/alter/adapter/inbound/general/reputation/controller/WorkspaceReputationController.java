package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.WorkspaceCreateReputationRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceCreateReputationUseCase;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/reputations")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class WorkspaceReputationController implements WorkspaceReputationControllerSpec {

    private final WorkspaceCreateReputationUseCase workspaceCreateReputation;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createReputation(
        @Valid @RequestBody WorkspaceCreateReputationRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        workspaceCreateReputation.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
