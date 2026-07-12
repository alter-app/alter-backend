package com.dreamteam.alter.adapter.inbound.admin.workspace.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminBusinessTypeRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminBusinessTypeResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.domain.workspace.command.AdminCreateBusinessTypeCommand;
import com.dreamteam.alter.domain.workspace.command.AdminUpdateBusinessTypeCommand;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminCreateBusinessTypeUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminDeleteBusinessTypeUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminGetBusinessTypeListUseCase;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminUpdateBusinessTypeUseCase;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/business-types")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminBusinessTypeController implements AdminBusinessTypeControllerSpec {

    @Resource(name = "adminGetBusinessTypeList")
    private final AdminGetBusinessTypeListUseCase adminGetBusinessTypeList;

    @Resource(name = "adminCreateBusinessType")
    private final AdminCreateBusinessTypeUseCase adminCreateBusinessType;

    @Resource(name = "adminUpdateBusinessType")
    private final AdminUpdateBusinessTypeUseCase adminUpdateBusinessType;

    @Resource(name = "adminDeleteBusinessType")
    private final AdminDeleteBusinessTypeUseCase adminDeleteBusinessType;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<AdminBusinessTypeResponseDto>>> getBusinessTypeList() {
        List<AdminBusinessTypeResponseDto> result = adminGetBusinessTypeList.execute().stream()
            .map(AdminBusinessTypeResponseDto::from)
            .toList();
        return ResponseEntity.ok(CommonApiResponse.of(result));
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<AdminBusinessTypeResponseDto>> createBusinessType(
        @Valid @RequestBody AdminBusinessTypeRequestDto request
    ) {
        Long id = adminCreateBusinessType.execute(
            new AdminCreateBusinessTypeCommand(request.getName(), request.getDescription())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonApiResponse.of(
                AdminBusinessTypeResponseDto.of(id, request.getName(), request.getDescription())
            ));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> updateBusinessType(
        @PathVariable Long id,
        @Valid @RequestBody AdminBusinessTypeRequestDto request
    ) {
        adminUpdateBusinessType.execute(
            id, new AdminUpdateBusinessTypeCommand(request.getName(), request.getDescription())
        );
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> deleteBusinessType(
        @PathVariable Long id
    ) {
        adminDeleteBusinessType.execute(id);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
