package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
public class UserReputationRequestFilterDto {

    @Parameter
    private ReputationRequestStatus status;

}
