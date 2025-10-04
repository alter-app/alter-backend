package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "사용자 공고 지원 목록 필터 DTO")
public class UserPostingApplicationListFilterDto {

    @Parameter(description = "지원 상태 (in)")
    private Set<PostingApplicationStatus> status;

}
