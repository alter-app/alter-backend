package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.*;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyInvitationListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getMyInvitationList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyInvitationList implements GetMyInvitationListUseCase {

    private final BusinessInvitationQueryRepository businessInvitationQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<MyInvitationResponseDto> execute(AppActor actor, MyInvitationListFilterDto filter, CursorPageRequestDto cursorPageRequest) {
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(cursorPageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(cursorPageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(cursorDto, cursorPageRequest.pageSize());

        long count = businessInvitationQueryRepository.countByUser(actor.getUser(), filter);
        if (count == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) count));
        }

        List<BusinessInvitation> invitations = businessInvitationQueryRepository.findByUserWithCursor(pageRequest, actor.getUser(), filter);
        if (ObjectUtils.isEmpty(invitations)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) count));
        }

        BusinessInvitation last = invitations.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(last.getId(), last.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            (int) count
        );

        return CursorPaginatedApiResponse.of(
            pageResponseDto,
            invitations.stream()
                .map(MyInvitationResponseDto::from)
                .toList()
        );
    }
}
