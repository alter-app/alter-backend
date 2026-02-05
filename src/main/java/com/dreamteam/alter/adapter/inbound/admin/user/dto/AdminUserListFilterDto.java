package com.dreamteam.alter.adapter.inbound.admin.user.dto;

import com.dreamteam.alter.domain.user.type.UserRole;
import com.dreamteam.alter.domain.user.type.UserStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "회원 목록 필터 DTO")
public class AdminUserListFilterDto {

    @Parameter(description = "회원 상태")
    private UserStatus status;

    @Parameter(description = "회원 역할")
    private UserRole role;

    @Parameter(description = "이메일 검색어")
    private String email;

    @Parameter(description = "이름 검색어")
    private String name;

    @Parameter(description = "닉네임 검색어")
    private String nickname;

    @Parameter(description = "연락처 검색어")
    private String contact;
}
