package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.domain.reputation.type.ReputationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RequesterInfo {

    @NotNull
    @Schema(description = "요청자 타입")
    private ReputationType type;

    @NotNull
    @Schema(description = "요청자 ID")
    private Long id;

    @NotBlank
    @Schema(description = "요청자(사용자/업장) 이름")
    private String name;

    public static RequesterInfo of(ReputationType type, Long id, String name) {
        return RequesterInfo.builder()
            .type(type)
            .id(id)
            .name(name)
            .build();
    }
}
