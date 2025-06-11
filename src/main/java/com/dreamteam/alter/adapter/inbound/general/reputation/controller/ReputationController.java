package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationRequestDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.reputation.port.inbound.CreateReputationRequestUseCase;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/reputations")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')") // TODO: 권한 세부 설정
@RequiredArgsConstructor
@Validated
public class ReputationController implements ReputationControllerSpec {

    @Resource(name = "createReputationRequest")
    private final CreateReputationRequestUseCase createReputationRequest;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> createReputationRequest(CreateReputationRequestDto request) {
        AppActor actor = AppActionContext.getInstance().getActor();

        createReputationRequest.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    // 자신에게 온 평판 작성 요청 목록 조회 (사용자)

    // 평판 요청 취소 (요청자)

    // 평판 요청 승인/거절 (요청받은 사람)

}
