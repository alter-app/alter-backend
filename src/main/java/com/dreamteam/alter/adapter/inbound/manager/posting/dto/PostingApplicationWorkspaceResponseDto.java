package com.dreamteam.alter.adapter.inbound.manager.posting.dto;

import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingApplicationWorkspaceResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "공고 지원 목록 업장 간략 정보 응답 DTO")
public class PostingApplicationWorkspaceResponseDto {

    @Schema(description = "업장 ID", example = "1")
    private Long id;

    @Schema(description = "업장 이름", example = "세븐일레븐 동양공대점")
    private String name;

    public static PostingApplicationWorkspaceResponseDto from(PostingApplicationWorkspaceResponse entity) {
        return PostingApplicationWorkspaceResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

}
