package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.domain.posting.type.PostingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저 공고 상태 변경 요청 DTO")
public class UpdatePostingStatusRequestDto {

    @NotNull
    @Schema(description = "공고 상태", example = "CLOSED")
    private PostingStatus status;
}
