package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyInvitationListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getMyInvitationList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyInvitationList implements GetMyInvitationListUseCase {

    private final BusinessInvitationQueryRepository businessInvitationQueryRepository;

    @Override
    public List<MyInvitationResponseDto> execute(AppActor actor) {
        return businessInvitationQueryRepository.findPendingByUser(actor.getUser()).stream()
            .map(MyInvitationResponseDto::from)
            .toList();
    }
}
