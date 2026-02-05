package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListFilterDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.AdminUserListResponse;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.port.inbound.AdminGetUserListUseCase;
import com.dreamteam.alter.domain.user.port.outbound.AdminUserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("adminGetUserList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetUserList implements AdminGetUserListUseCase {

    private final AdminUserQueryRepository adminUserQueryRepository;

    @Override
    public PaginatedResponseDto<AdminUserListResponseDto> execute(
        PageRequestDto request,
        AdminUserListFilterDto filter,
        AdminActor actor
    ) {
        long count = adminUserQueryRepository.getUserCount(filter);
        if (count == 0) {
            return PaginatedResponseDto.empty(PageResponseDto.empty(request));
        }

        List<AdminUserListResponse> users = adminUserQueryRepository.getUserListUsingPagination(request, filter);

        PageResponseDto pageResponseDto = PageResponseDto.of(request, (int) count);

        return PaginatedResponseDto.of(
            pageResponseDto,
            users.stream()
                .map(AdminUserListResponseDto::from)
                .toList()
        );
    }
}
