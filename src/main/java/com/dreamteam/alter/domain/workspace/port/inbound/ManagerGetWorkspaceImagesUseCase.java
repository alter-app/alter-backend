package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceImageResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetWorkspaceImagesUseCase {
    List<WorkspaceImageResponseDto> execute(ManagerActor actor, Long workspaceId);

    // 호출 측에서 이미 업장 소유를 검증한 경우 사용 (소유 재검증 생략)
    List<WorkspaceImageResponseDto> getImagesWithoutOwnershipCheck(Long workspaceId);
}
