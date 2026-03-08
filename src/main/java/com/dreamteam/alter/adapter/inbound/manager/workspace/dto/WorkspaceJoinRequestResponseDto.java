package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
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
        return WorkspaceJoinRequestResponseDto.builder()
            .joinRequestId(request.getId())
            .userName(request.getUser().getName())
            .userContact(request.getUser().getContact())
            .status(request.getStatus())
            .requestedAt(request.getCreatedAt())
            .build();
    }
}
