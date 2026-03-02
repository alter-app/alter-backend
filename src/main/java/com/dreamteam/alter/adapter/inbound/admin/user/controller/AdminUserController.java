package com.dreamteam.alter.adapter.inbound.admin.user.controller;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserPasswordRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserDetailResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListFilterDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.application.aop.AdminActionContext;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.port.inbound.AdminGetUserDetailUseCase;
import com.dreamteam.alter.domain.user.port.inbound.AdminGetUserListUseCase;
import com.dreamteam.alter.domain.user.port.inbound.AdminUpdateUserPasswordUseCase;
import com.dreamteam.alter.domain.user.port.inbound.AdminUpdateUserStatusUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminUserController implements AdminUserControllerSpec {

    @Resource(name = "adminGetUserList")
    private final AdminGetUserListUseCase adminGetUserList;

    @Resource(name = "adminGetUserDetail")
    private final AdminGetUserDetailUseCase adminGetUserDetail;

    @Resource(name = "adminUpdateUserPassword")
    private final AdminUpdateUserPasswordUseCase adminUpdateUserPassword;

    @Resource(name = "adminUpdateUserStatus")
    private final AdminUpdateUserStatusUseCase adminUpdateUserStatus;

    @Override
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<AdminUserListResponseDto>> getUserList(
        PageRequestDto request,
        AdminUserListFilterDto filter
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();

        return ResponseEntity.ok(adminGetUserList.execute(request, filter, actor));
    }

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<CommonApiResponse<AdminUserDetailResponseDto>> getUserDetail(
        @PathVariable Long userId
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(adminGetUserDetail.execute(userId, actor)));
    }

    @Override
    @PutMapping("/{userId}/password")
    public ResponseEntity<CommonApiResponse<Void>> updateUserPassword(
        @PathVariable Long userId,
        @Valid @RequestBody AdminUpdateUserPasswordRequestDto request
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();

        adminUpdateUserPassword.execute(userId, request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @PutMapping("/{userId}/status")
    public ResponseEntity<CommonApiResponse<Void>> updateUserStatus(
        @PathVariable Long userId,
        @Valid @RequestBody AdminUpdateUserStatusRequestDto request
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();

        adminUpdateUserStatus.execute(userId, request, actor);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
