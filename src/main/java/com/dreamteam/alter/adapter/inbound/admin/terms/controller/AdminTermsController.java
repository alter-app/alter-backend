package com.dreamteam.alter.adapter.inbound.admin.terms.controller;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminCreateTermsRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminCreateTermsResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsListItemResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminUpdateTermsRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.domain.terms.command.AdminCreateTermsCommand;
import com.dreamteam.alter.domain.terms.command.AdminUpdateTermsCommand;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.inbound.AdminCreateTermsUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsDetailUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminGetTermsListUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminPublishTermsUseCase;
import com.dreamteam.alter.domain.terms.port.inbound.AdminUpdateTermsUseCase;
import com.dreamteam.alter.domain.terms.query.AdminGetTermsListQuery;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
        AdminGetTermsListQuery query = new AdminGetTermsListQuery(
                filter.getType(), filter.getStatus(), pageRequest.page(), pageRequest.pageSize());

        return ResponseEntity.ok(adminGetTermsList.execute(query));
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<AdminCreateTermsResponseDto>> createTerms(
        @Valid @RequestBody AdminCreateTermsRequestDto request
    ) {
        AdminCreateTermsCommand command = new AdminCreateTermsCommand(
                request.getType(), request.getVersion(), request.getTitle(),
                request.getDocUrl(), request.isRequired());
        Long id = adminCreateTerms.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.of(AdminCreateTermsResponseDto.of(id)));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<AdminTermsDetailResponseDto>> getTermsDetail(
        @PathVariable Long id
    ) {
        Terms terms = adminGetTermsDetail.execute(id);
        return ResponseEntity.ok(CommonApiResponse.of(AdminTermsDetailResponseDto.from(terms)));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> updateTerms(
        @PathVariable Long id,
        @Valid @RequestBody AdminUpdateTermsRequestDto request
    ) {
        AdminUpdateTermsCommand command = new AdminUpdateTermsCommand(
                request.getTitle(), request.getDocUrl(), request.isRequired());
        adminUpdateTerms.execute(id, command);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<CommonApiResponse<Void>> publishTerms(
        @PathVariable Long id
    ) {
        adminPublishTerms.execute(id);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
