package com.dreamteam.alter.adapter.inbound.admin.terms.controller;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminCreateTermsRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminUpdateTermsRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.application.aop.AdminActionContext;
import com.dreamteam.alter.domain.terms.port.inbound.AdminCreateTermsUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsDetailUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsListUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminPublishTermsUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminUpdateTermsUseCase;
import com.dreamteam.alter.domain.user.context.AdminActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/terms")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminTermsController implements AdminTermsControllerSpec {

    @Resource(name = "adminGetTermsList")
    private final AdminGetTermsListUseCase adminGetTermsList;

    @Resource(name = "adminCreateTerms")
    private final AdminCreateTermsUseCase adminCreateTerms;

    @Resource(name = "adminGetTermsDetail")
    private final AdminGetTermsDetailUseCase adminGetTermsDetail;

    @Resource(name = "adminUpdateTerms")
    private final AdminUpdateTermsUseCase adminUpdateTerms;

    @Resource(name = "adminPublishTerms")
    private final AdminPublishTermsUseCase adminPublishTerms;

    @Override
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<AdminTermsListItemResponseDto>> getTermsList(
        PageRequestDto pageRequest,
        TermsListFilterDto filter
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        return ResponseEntity.ok(adminGetTermsList.execute(filter, pageRequest, actor));
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<Map<String, Long>>> createTerms(
        @Valid @RequestBody AdminCreateTermsRequestDto request
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        Long id = adminCreateTerms.execute(request, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.of(Map.of("id", id)));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<AdminTermsDetailResponseDto>> getTermsDetail(
        @PathVariable Long id
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(adminGetTermsDetail.execute(id, actor)));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> updateTerms(
        @PathVariable Long id,
        @Valid @RequestBody AdminUpdateTermsRequestDto request
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        adminUpdateTerms.execute(id, request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<CommonApiResponse<Void>> publishTerms(
        @PathVariable Long id
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        adminPublishTerms.execute(id, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
