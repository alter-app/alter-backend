package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 - 근무자 정보 응답 DTO")
public class UserWorkspaceWorkerResponseDto {

    @NotNull
    @Schema(description = "근무자 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "근무자 이름", example = "홍길동")
    private String name;

    public static UserWorkspaceWorkerResponseDto of(UserWorkspaceWorkerResponse entity) {
        return UserWorkspaceWorkerResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .build();
    }

}
