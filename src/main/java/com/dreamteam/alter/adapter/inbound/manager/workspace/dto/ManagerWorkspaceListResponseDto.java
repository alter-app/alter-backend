package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceListResponse;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 - 관리중인 업장 목록 조회 응답 DTO")
public class ManagerWorkspaceListResponseDto {

    @NotNull
    @Schema(description = "업장 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "업장 이름", example = "세븐일레븐")
    private String businessName;

    @NotBlank
    @Schema(description = "업장 전체 주소", example = "서울특별시 강남구 테헤란로 123")
    private String fullAddress;

    @NotNull
    @Schema(description = "등록일")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "업장 상태")
    private DescribedEnumDto<WorkspaceStatus> status;

    public static ManagerWorkspaceListResponseDto of(ManagerWorkspaceListResponse entity) {
        return ManagerWorkspaceListResponseDto.builder()
                .id(entity.getId())
                .businessName(entity.getBusinessName())
                .fullAddress(entity.getFullAddress())
                .createdAt(entity.getCreatedAt())
                .status(DescribedEnumDto.of(entity.getStatus(), WorkspaceStatus.describe()))
                .build();
    }

}
