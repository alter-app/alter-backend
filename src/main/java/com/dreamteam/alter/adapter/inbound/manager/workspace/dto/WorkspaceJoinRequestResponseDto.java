package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "업장 합류 요청 응답 DTO")
public class WorkspaceJoinRequestResponseDto {

    @Schema(description = "합류 요청 ID", example = "1")
    private Long joinRequestId;

    @Schema(description = "요청자 이름", example = "김철수")
    private String userName;

    @Schema(description = "요청자 휴대폰 번호", example = "01012345678")
    private String userContact;

    @Schema(description = "합류 요청 상태", example = "PENDING")
    private BusinessJoinRequestStatus status;

    @Schema(description = "합류 요청 일시", example = "2024-03-01T10:00:00")
    private LocalDateTime requestedAt;

    public static WorkspaceJoinRequestResponseDto from(BusinessJoinRequest request) {
        return new WorkspaceJoinRequestResponseDto(
            request.getId(),
            request.getUser().getName(),
            request.getUser().getContact(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}
