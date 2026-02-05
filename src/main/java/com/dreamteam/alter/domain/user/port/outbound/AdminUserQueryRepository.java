package com.dreamteam.alter.domain.user.port.outbound;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.AdminUserDetailResponse;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.AdminUserListResponse;

import java.util.List;
import java.util.Optional;

public interface AdminUserQueryRepository {
    long getUserCount(AdminUserListFilterDto filter);

    List<AdminUserListResponse> getUserListUsingPagination(
        PageRequestDto pageRequest,
        AdminUserListFilterDto filter
    );

    Optional<AdminUserDetailResponse> getUserDetail(Long userId);
}
