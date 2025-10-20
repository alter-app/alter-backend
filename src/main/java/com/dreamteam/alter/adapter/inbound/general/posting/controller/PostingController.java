package com.dreamteam.alter.adapter.inbound.general.posting.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.*;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.posting.port.inbound.*;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/postings")
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
public class PostingController implements PostingControllerSpec {

    @Resource(name = "getPostingsWithCursor")
    private final GetPostingsWithCursorUseCase getPostingsWithCursor;

    @Resource(name = "getPostingDetail")
    private final GetPostingDetailUseCase getPostingDetail;

    @Resource(name = "createPostingApplication")
    private final CreatePostingApplicationUseCase createPostingApplication;

    @Resource(name = "getPostingFilterOptions")
    private final GetPostingFilterOptionsUseCase getPostingFilterOptions;

    @Resource(name = "getPostingMapList")
    private final GetPostingMapListUseCase getPostingMapList;

    @Resource(name = "getPostingMapMarkers")
    private final GetPostingMapMarkersUseCase getPostingMapMarkers;

    @Resource(name = "getWorkspacePostingList")
    private final GetWorkspacePostingListUseCase getWorkspacePostingList;

    @Override
    @GetMapping
    public ResponseEntity<CursorPaginatedApiResponse<PostingListResponseDto>> getPostingsWithCursor(
        CursorPageRequestDto request,
        PostingListFilterDto filter
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(getPostingsWithCursor.execute(request, filter, actor));
    }

    @Override
    @GetMapping("/{postingId}")
    public ResponseEntity<CommonApiResponse<PostingDetailResponseDto>> getPostingDetail(
        @PathVariable Long postingId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getPostingDetail.execute(postingId, actor)));
    }

    @Override
    @PostMapping("/apply/{postingId}")
    public ResponseEntity<CommonApiResponse<Void>> applyIntoPosting(
        @PathVariable Long postingId,
        CreatePostingApplicationRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        createPostingApplication.execute(actor, postingId, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/filter-options")
    public ResponseEntity<CommonApiResponse<PostingFilterOptionsResponseDto>> getPostingFilterOptions() {
        return ResponseEntity.ok(CommonApiResponse.of(getPostingFilterOptions.execute()));
    }

    @Override
    @GetMapping("/map")
    public ResponseEntity<CursorPaginatedApiResponse<PostingMapListResponseDto>> getPostingMapList(
        CursorPageRequestDto request,
        PostingMapListFilterDto filter
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(getPostingMapList.execute(request, filter, actor));
    }

    @Override
    @GetMapping("/map/markers")
    public ResponseEntity<CommonApiResponse<List<PostingMapMarkerResponseDto>>> getPostingMapMarkers(
        PostingMapMarkerFilterDto filter
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(getPostingMapMarkers.execute(filter)));
    }

    @Override
    @GetMapping("/workspaces/{workspaceId}")
    public ResponseEntity<CommonApiResponse<List<PostingListResponseDto>>> getWorkspacePostingList(
        @PathVariable Long workspaceId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getWorkspacePostingList.execute(workspaceId, actor)));
    }

}
