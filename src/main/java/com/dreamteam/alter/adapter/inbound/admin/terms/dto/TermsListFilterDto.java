package com.dreamteam.alter.adapter.inbound.admin.terms.dto;

import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "약관 목록 필터 DTO")
public class TermsListFilterDto {

    @Parameter(description = "약관 유형")
    private TermsType type;

    @Parameter(description = "약관 상태")
    private TermsStatus status;
}
