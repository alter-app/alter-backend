package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import com.dreamteam.alter.domain.reputation.type.ReputationKeywordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "사용 가능한 평판 키워드 요청 DTO")
public class AvailableReputationKeywordRequestDto {

    @Schema(description = "평판 키워드 유형")
    private ReputationKeywordType keywordType;

}
