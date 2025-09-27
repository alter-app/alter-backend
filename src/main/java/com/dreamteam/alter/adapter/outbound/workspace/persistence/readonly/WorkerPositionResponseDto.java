package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.workspace.type.WorkerPositionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkerPositionResponseDto {

    @Enumerated(EnumType.STRING)
    private WorkerPositionType type;

    private String description;

    private String emoji;

    public static WorkerPositionResponseDto from(WorkerPositionType positionType) {
        return new WorkerPositionResponseDto(
            positionType,
            positionType.getDescription(),
            positionType.getEmoji()
        );
    }

}
