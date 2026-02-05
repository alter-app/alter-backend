package com.dreamteam.alter.adapter.inbound.admin.user.dto;

import com.dreamteam.alter.domain.user.type.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 상태 변경 요청 DTO")
public class AdminUpdateUserStatusRequestDto {

    @NotNull
    @Schema(description = "변경할 상태")
    private UserStatus status;
}
