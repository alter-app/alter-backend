package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.domain.posting.type.PostingStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "매니저 공고 목록 조회 필터 DTO")
public class ManagerPostingListFilterDto {

    @Parameter(description = "업장 ID (eq)")
    private Long workspaceId;

    @Parameter(description = "공고 상태 (eq)")
    private PostingStatus status;

}
