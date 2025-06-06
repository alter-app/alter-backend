package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 공고 지원 상태 업데이트 요청 DTO")
public class UpdateUserPostingApplicationStatusRequestDto {

    @NotNull
    @Schema(description = "지원 상태", example = "CANCELLED")
    private PostingApplicationStatus status;

}
