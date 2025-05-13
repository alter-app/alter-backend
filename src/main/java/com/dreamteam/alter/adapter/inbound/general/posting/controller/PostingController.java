package com.dreamteam.alter.adapter.inbound.general.posting.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createPosting(
        CreatePostingRequestDto request
    ) {
        createPosting.execute(request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

}
