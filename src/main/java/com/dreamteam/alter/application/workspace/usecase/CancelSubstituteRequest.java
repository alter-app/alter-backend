package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.CancelSubstituteRequestUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("cancelSubstituteRequest")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CancelSubstituteRequest implements CancelSubstituteRequestUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Override
    public void execute(AppActor actor, Long requestId) {
        // 대타 요청 조회
        SubstituteRequest request = substituteRequestQueryRepository.findById(requestId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 대타 요청입니다."));

        // 현재 사용자의 WorkspaceWorker 정보 조회
        WorkspaceWorker myWorker = workspaceWorkerQueryRepository
            .findActiveWorkerByWorkspaceAndUser(request.getWorkspaceShift().getWorkspace(), actor.getUser())
            .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 근무자가 아닙니다."));

        // 취소 가능 여부 검증 (요청자 본인이고, PENDING 또는 ACCEPTED 상태)
        if (!request.canBeCancelledBy(myWorker.getId())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "취소할 수 없는 요청입니다.");
        }

        // 취소 처리
        request.cancel();

        // TODO: 알림 발송 (수락한 근무자와 점주/매니저에게)
    }
}
