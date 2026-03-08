package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyInvitationListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service("getMyInvitationList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyInvitationList implements GetMyInvitationListUseCase {

    private final BusinessInvitationQueryRepository businessInvitationQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<MyInvitationResponseDto> execute(AppActor actor, BusinessInvitationStatus status, LocalDate from, LocalDate to, CursorPageRequestDto cursorPageRequest) {
        CursorDto cursor = StringUtils.hasText(cursorPageRequest.cursor())
            ? CursorUtil.decodeCursor(cursorPageRequest.cursor(), CursorDto.class, objectMapper)
            : null;

        List<BusinessInvitation> invitations = businessInvitationQueryRepository.findByUserWithCursor(
            actor.getUser(), status,
            from != null ? from.atStartOfDay() : null,
            to != null ? to.plusDays(1).atStartOfDay() : null,
            cursor, cursorPageRequest.pageSize() + 1
        );

        boolean hasNext = invitations.size() > cursorPageRequest.pageSize();
        List<BusinessInvitation> pageItems = hasNext
            ? invitations.subList(0, cursorPageRequest.pageSize())
            : invitations;

        String nextCursor = hasNext
            ? CursorUtil.encodeCursor(
                new CursorDto(pageItems.getLast().getId(), pageItems.getLast().getCreatedAt()),
                objectMapper)
            : null;

        List<MyInvitationResponseDto> data = pageItems.stream()
            .map(MyInvitationResponseDto::from)
            .toList();

        return CursorPaginatedApiResponse.of(
            CursorPageResponseDto.of(nextCursor, cursorPageRequest.pageSize(), data.size()),
            data
        );
    }
}
