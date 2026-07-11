package com.dreamteam.alter.adapter.inbound.admin.posting.controller;

import com.dreamteam.alter.adapter.inbound.admin.posting.dto.AdminPostingKeywordRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.posting.dto.AdminPostingKeywordResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.domain.posting.command.AdminCreatePostingKeywordCommand;
import com.dreamteam.alter.domain.posting.command.AdminUpdatePostingKeywordCommand;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.AdminCreatePostingKeywordUseCase;
import com.dreamteam.alter.domain.posting.port.inbound.AdminDeletePostingKeywordUseCase;
import com.dreamteam.alter.domain.posting.port.inbound.AdminGetPostingKeywordListUseCase;
import com.dreamteam.alter.domain.posting.port.inbound.AdminUpdatePostingKeywordUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/posting-keywords")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminPostingKeywordController implements AdminPostingKeywordControllerSpec {

    @Resource(name = "adminGetPostingKeywordList")
    private final AdminGetPostingKeywordListUseCase adminGetPostingKeywordList;

    @Resource(name = "adminCreatePostingKeyword")
    private final AdminCreatePostingKeywordUseCase adminCreatePostingKeyword;

    @Resource(name = "adminUpdatePostingKeyword")
    private final AdminUpdatePostingKeywordUseCase adminUpdatePostingKeyword;

    @Resource(name = "adminDeletePostingKeyword")
    private final AdminDeletePostingKeywordUseCase adminDeletePostingKeyword;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<AdminPostingKeywordResponseDto>>> getPostingKeywordList() {
        List<AdminPostingKeywordResponseDto> result = adminGetPostingKeywordList.execute().stream()
            .map(AdminPostingKeywordResponseDto::from)
            .toList();
        return ResponseEntity.ok(CommonApiResponse.of(result));
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<AdminPostingKeywordResponseDto>> createPostingKeyword(
        @Valid @RequestBody AdminPostingKeywordRequestDto request
    ) {
        PostingKeyword created = adminCreatePostingKeyword.execute(
            new AdminCreatePostingKeywordCommand(request.getName(), request.getDescription())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonApiResponse.of(AdminPostingKeywordResponseDto.from(created)));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> updatePostingKeyword(
        @PathVariable Long id,
        @Valid @RequestBody AdminPostingKeywordRequestDto request
    ) {
        adminUpdatePostingKeyword.execute(
            id, new AdminUpdatePostingKeywordCommand(request.getName(), request.getDescription())
        );
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> deletePostingKeyword(
        @PathVariable Long id
    ) {
        adminDeletePostingKeyword.execute(id);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
