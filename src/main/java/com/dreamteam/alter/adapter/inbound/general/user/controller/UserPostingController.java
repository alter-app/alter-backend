package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UpdateUserPostingApplicationStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UserPostingApplicationListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserFavoritePostingListResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.*;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/users/me/postings")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')") // TODO: 권한 세부 설정
@RequiredArgsConstructor
@Validated
public class UserPostingController implements UserPostingControllerSpec {

    @Resource(name = "getUserPostingApplicationList")
    private final GetUserPostingApplicationListUseCase getUserPostingApplicationList;

    @Resource(name = "updateUserPostingApplicationStatus")
    private final UpdateUserPostingApplicationStatusUseCase updateUserPostingApplicationStatus;

    @Resource(name = "createUserFavoritePosting")
    private final CreateUserFavoritePostingUseCase createUserFavoritePosting;

    @Resource(name = "getUserFavoritePostingList")
    private final GetUserFavoritePostingListUseCase getUserFavoritePostingList;

    @Resource(name = "deleteUserFavoritePosting")
    private final DeleteUserFavoritePostingUseCase deleteUserFavoritePosting;

    @Override
    @GetMapping("/applications")
    public ResponseEntity<PaginatedResponseDto<UserPostingApplicationListResponseDto>> getMyPostingApplications(
        PageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(getUserPostingApplicationList.execute(actor, pageRequest));
    }

    @Override
    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<CommonApiResponse<Void>> updateMyPostingApplicationStatus(
        @PathVariable Long applicationId,
        UpdateUserPostingApplicationStatusRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        updateUserPostingApplicationStatus.execute(actor, applicationId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PostMapping("/favorites/{postingId}")
    public ResponseEntity<CommonApiResponse<Void>> addUserFavoritePosting(@PathVariable Long postingId) {
        AppActor actor = AppActionContext.getInstance().getActor();

        createUserFavoritePosting.execute(actor, postingId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/favorites")
    public ResponseEntity<CursorPaginatedApiResponse<UserFavoritePostingListResponseDto>> getUserFavoritePostingList(
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(getUserFavoritePostingList.execute(actor, pageRequest));
    }

    @Override
    @DeleteMapping("/favorites/{postingId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteUserFavoritePosting(@PathVariable Long postingId) {
        AppActor actor = AppActionContext.getInstance().getActor();

        deleteUserFavoritePosting.execute(actor, postingId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
