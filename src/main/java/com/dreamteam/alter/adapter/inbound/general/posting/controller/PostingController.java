package com.dreamteam.alter.adapter.inbound.general.posting.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListResponseDto;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.inbound.GetPostingsWithCursorUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
