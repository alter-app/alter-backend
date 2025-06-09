package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "공고 리스트 조회 업장 정보 응답 DTO")
public class PostingListWorkspaceResponseDto {

    @NotNull
    @Schema(description = "업장 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "업장 이름", example = "카페 알터")
    private String businessName;

    public static PostingListWorkspaceResponseDto from(Workspace entity) {
        return PostingListWorkspaceResponseDto.builder()
            .id(entity.getId())
            .businessName(entity.getBusinessName())
            .build();
    }

}
