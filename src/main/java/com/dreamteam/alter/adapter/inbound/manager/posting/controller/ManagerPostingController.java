package com.dreamteam.alter.adapter.inbound.manager.posting.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerGetPostingApplicationDetailUseCase;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerGetPostingApplicationListWithCursorUseCase;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingApplicationStatusUseCase;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/postings")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerPostingController implements ManagerPostingControllerSpec {

    @Resource(name = "managerGetPostingApplicationListWithCursor")
    private final ManagerGetPostingApplicationListWithCursorUseCase managerGetPostingApplicationListWithCursor;

    @Resource(name = "managerGetPostingApplicationDetail")
    private final ManagerGetPostingApplicationDetailUseCase managerGetPostingApplicationDetail;

    @Resource(name = "managerUpdatePostingApplicationStatus")
    private final ManagerUpdatePostingApplicationStatusUseCase managerUpdatePostingApplicationStatus;

    @Override
    @GetMapping("/applications")
    public ResponseEntity<CursorPaginatedApiResponse<PostingApplicationListResponseDto>> getPostingApplicationListWithCursor(
        CursorPageRequestDto request,
        PostingApplicationListFilterDto filter
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(managerGetPostingApplicationListWithCursor.execute(request,filter, actor));
    }

    @Override
    @GetMapping("/applications/{postingApplicationId}")
    public ResponseEntity<CommonApiResponse<PostingApplicationResponseDto>> getPostingApplicationDetail(
        @PathVariable Long postingApplicationId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetPostingApplicationDetail.execute(postingApplicationId, actor)));
    }

    @Override
    @PatchMapping("/applications/{postingApplicationId}/status")
    public ResponseEntity<CommonApiResponse<Void>> updatePostingApplicationStatus(
        @PathVariable Long postingApplicationId,
        UpdatePostingApplicationStatusRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        managerUpdatePostingApplicationStatus.execute(postingApplicationId, request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
