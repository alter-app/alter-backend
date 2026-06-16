package com.dreamteam.alter.adapter.inbound.common.dto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "fileId")
@Schema(description = "업장 대표이미지 요청 항목 DTO")
public class WorkspaceImageRequestDto {

    @NotBlank
    @Schema(description = "대표이미지 파일 ID", example = "01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e")
    private String fileId;

    @Schema(description = "노출 순서 (작을수록 먼저, 미지정 시 마지막)", example = "0")
    private Integer sortOrder;

    /**
     * sortOrder 오름차순(미지정은 후순위)으로 정렬한 파일 ID 목록을 반환한다.
     * fileId 기준 equals/hashCode 로 Set 단계에서 이미 중복이 제거된다.
     */
    public static List<String> toOrderedFileIds(Collection<WorkspaceImageRequestDto> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
            .sorted(Comparator
                .comparing(WorkspaceImageRequestDto::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(WorkspaceImageRequestDto::getFileId))
            .map(WorkspaceImageRequestDto::getFileId)
            .toList();
    }
}
