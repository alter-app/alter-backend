package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UserPostingApplicationListResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.GetUserPostingApplicationListUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UpdateUserPostingApplicationStatusUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/users/me/postings/applications")
@RequiredArgsConstructor
@Validated
public class UserPostingController implements UserPostingControllerSpec {

    @Resource(name = "getUserPostingApplicationList")
    private final GetUserPostingApplicationListUseCase getUserPostingApplicationList;

    @Resource(name = "updateUserPostingApplicationStatus")
    private final UpdateUserPostingApplicationStatusUseCase updateUserPostingApplicationStatus;

    @Override
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<UserPostingApplicationListResponseDto>> getMyPostingApplications(
        PageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(getUserPostingApplicationList.execute(actor, pageRequest));
    }

    @Override
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<CommonApiResponse<Void>> updateMyPostingApplicationStatus(
        @PathVariable Long applicationId,
        UpdateUserPostingApplicationStatusRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        updateUserPostingApplicationStatus.execute(actor, applicationId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
