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
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')") // TODO: 권한 세부 설정
@RequiredArgsConstructor
@Validated
public class PostingController implements PostingControllerSpec {

    @Resource(name = "createPosting")
    private final CreatePostingUseCase createPosting;

    @Resource(name = "getPostingsWithCursor")
    private final GetPostingsWithCursorUseCase getPostingsWithCursor;

    @Resource(name = "getPostingDetail")
    private final GetPostingDetailUseCase getPostingDetail;

    @Resource(name = "getPostingKeywordList")
    private final GetPostingKeywordListUseCase getPostingKeywordList;

    @Resource(name = "createPostingApplication")
    private final CreatePostingApplicationUseCase createPostingApplication;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createPosting(
        CreatePostingRequestDto request
    ) {
        createPosting.execute(request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping
    public ResponseEntity<CursorPaginatedApiResponse<PostingListResponseDto>> getPostingsWithCursor(
        CursorPageRequestDto request
    ) {
        return ResponseEntity.ok(getPostingsWithCursor.execute(request));
    }

    @Override
    @GetMapping("/{postingId}")
    public ResponseEntity<CommonApiResponse<PostingDetailResponseDto>> getPostingDetail(
        @PathVariable Long postingId
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(getPostingDetail.execute(postingId)));
    }

    @Override
    @GetMapping("/available-keywords")
    public ResponseEntity<CommonApiResponse<List<PostingKeywordListResponseDto>>> getAvailablePostingKeywords() {
        return ResponseEntity.ok(CommonApiResponse.of(getPostingKeywordList.execute()));
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

}
