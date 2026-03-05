package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyJoinRequestListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getMyJoinRequestList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyJoinRequestList implements GetMyJoinRequestListUseCase {

    private final BusinessJoinRequestQueryRepository businessJoinRequestQueryRepository;

    @Override
    public List<MyJoinRequestResponseDto> execute(AppActor actor) {
        return businessJoinRequestQueryRepository.findPendingByUser(actor.getUser()).stream()
            .map(MyJoinRequestResponseDto::from)
            .toList();
    }
}
