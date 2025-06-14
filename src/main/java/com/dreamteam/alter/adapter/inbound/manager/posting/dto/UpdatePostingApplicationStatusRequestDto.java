package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 관리자 공고 지원 상태 업데이트 요청 DTO")
public class UpdatePostingApplicationStatusRequestDto {

    @NotNull
    @Schema(description = "지원 상태", example = "ACCEPTED")
    private PostingApplicationStatus status;

}
