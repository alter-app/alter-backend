package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "평판 요청 수락 DTO")
public class AcceptReputationRequestDto {

    @NotNull
    @NotEmpty
    @Valid
    @Schema(description = "평판 키워드 목록 (2개 이상 6개 미만)")
    private Set<ReputationKeywordMapDto> keywords;

}
