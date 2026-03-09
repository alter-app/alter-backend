package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.port.inbound.GetMyInvitationListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service("getMyInvitationList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyInvitationList implements GetMyInvitationListUseCase {

    private final BusinessInvitationQueryRepository businessInvitationQueryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CursorPaginatedApiResponse<MyInvitationResponseDto> execute(AppActor actor, MyInvitationListFilterDto filter, CursorPageRequestDto cursorPageRequest) {
        BusinessInvitationStatus status = ObjectUtils.isNotEmpty(filter) ? filter.getStatus() : null;
        LocalDateTime from = ObjectUtils.isNotEmpty(filter) && filter.getFrom() != null ? filter.getFrom().atStartOfDay() : null;
        LocalDateTime to = ObjectUtils.isNotEmpty(filter) && filter.getTo() != null ? filter.getTo().plusDays(1).atStartOfDay() : null;

        long totalCount = businessInvitationQueryRepository.countByUser(actor.getUser(), status, from, to);
        if (totalCount == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) totalCount));
        }

        CursorDto cursor = StringUtils.hasText(cursorPageRequest.cursor())
            ? CursorUtil.decodeCursor(cursorPageRequest.cursor(), CursorDto.class, objectMapper)
            : null;

        List<BusinessInvitation> invitations = businessInvitationQueryRepository.findByUserWithCursor(
            actor.getUser(), status, from, to, cursor, cursorPageRequest.pageSize() + 1
        );

        if (ObjectUtils.isEmpty(invitations)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(cursorPageRequest.pageSize(), (int) totalCount));
        }

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
            CursorPageResponseDto.of(nextCursor, cursorPageRequest.pageSize(), (int) totalCount),
            data
        );
    }
}
