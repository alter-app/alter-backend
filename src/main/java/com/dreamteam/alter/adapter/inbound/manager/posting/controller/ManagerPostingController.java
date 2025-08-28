package com.dreamteam.alter.adapter.inbound.manager.posting.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingKeywordListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingDetailResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingStatusRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.posting.port.inbound.*;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager/postings")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerPostingController implements ManagerPostingControllerSpec {

    @Resource(name = "createPosting")
    private final CreatePostingUseCase createPosting;

    @Resource(name = "getPostingKeywordList")
    private final GetPostingKeywordListUseCase getPostingKeywordList;

    @Resource(name = "managerGetPostingApplicationListWithCursor")
    private final ManagerGetPostingApplicationListWithCursorUseCase managerGetPostingApplicationListWithCursor;

    @Resource(name = "managerGetPostingApplicationDetail")
    private final ManagerGetPostingApplicationDetailUseCase managerGetPostingApplicationDetail;

    @Resource(name = "managerUpdatePostingApplicationStatus")
    private final ManagerUpdatePostingApplicationStatusUseCase managerUpdatePostingApplicationStatus;

    @Resource(name = "managerGetPostingList")
    private final ManagerGetPostingListUseCase managerGetPostingList;

    @Resource(name = "managerGetPostingDetail")
    private final ManagerGetPostingDetailUseCase managerGetPostingDetail;

    @Resource(name = "managerUpdatePostingStatus")
    private final ManagerUpdatePostingStatusUseCase managerUpdatePostingStatus;

    @Resource(name = "managerUpdatePosting")
    private final ManagerUpdatePostingUseCase managerUpdatePosting;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createPosting(
        CreatePostingRequestDto request
    ) {
        createPosting.execute(request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/available-keywords")
    public ResponseEntity<CommonApiResponse<List<PostingKeywordListResponseDto>>> getAvailablePostingKeywords() {
        return ResponseEntity.ok(CommonApiResponse.of(getPostingKeywordList.execute()));
    }

    @Override
    @GetMapping
    public ResponseEntity<CursorPaginatedApiResponse<ManagerPostingListResponseDto>> getMyPostingsWithCursor(
        CursorPageRequestDto request,
        ManagerPostingListFilterDto filter
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(managerGetPostingList.execute(request, filter, actor));
    }

    @Override
    @GetMapping("/{postingId}")
    public ResponseEntity<CommonApiResponse<ManagerPostingDetailResponseDto>> getMyPostingDetail(
        @PathVariable Long postingId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(managerGetPostingDetail.execute(postingId, actor)));
    }

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

    @Override
    @PatchMapping("/{postingId}/status")
    public ResponseEntity<CommonApiResponse<Void>> updatePostingStatus(
        @PathVariable Long postingId,
        UpdatePostingStatusRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        managerUpdatePostingStatus.execute(postingId, request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/{postingId}")
    public ResponseEntity<CommonApiResponse<Void>> updatePosting(
        @PathVariable Long postingId,
        UpdatePostingRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        managerUpdatePosting.execute(postingId, request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
