package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceWorkerUserResponse;
import com.dreamteam.alter.domain.user.type.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 - 업장 근무자 정보 응답 DTO")
public class WorkspaceWorkerUserResponseDto {

    @NotNull
    @Schema(description = "근무자 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "근무자 이름", example = "홍길동")
    private String name;

    @NotBlank
    @Schema(description = "근무자 연락처", example = "010-1234-5678")
    private String contact;

    @NotNull
    @Schema(description = "근무자 성별", example = "GENDER_MALE")
    private UserGender gender;

    public static WorkspaceWorkerUserResponseDto of(WorkspaceWorkerUserResponse entity) {
        return WorkspaceWorkerUserResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .contact(entity.getContact())
            .gender(entity.getGender())
            .build();
    }

}
