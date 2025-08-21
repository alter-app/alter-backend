package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserReputationRequestFilterDto;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.domain.reputation.port.inbound.UserGetReputationRequestListUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userGetReputationRequestList")
public class UserGetReputationRequestList extends AbstractGetReputationRequestList<AppActor, UserReputationRequestFilterDto, Long>
    implements UserGetReputationRequestListUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;

    public UserGetReputationRequestList(ReputationRequestQueryRepository reputationRequestQueryRepository, ObjectMapper objectMapper) {
        super(objectMapper);
        this.reputationRequestQueryRepository = reputationRequestQueryRepository;
    }

    @Override
    protected Long prepare(AppActor actor, UserReputationRequestFilterDto filter) {
        return actor.getUser().getId();
    }

    @Override
    protected void validate(AppActor actor, UserReputationRequestFilterDto filter, Long userId) { }

    @Override
    protected long count(AppActor actor, UserReputationRequestFilterDto filter, Long userId) {
        ReputationRequestStatus status = (filter != null) ? filter.getStatus() : null;
        return reputationRequestQueryRepository.getCountOfReputationRequestsByUser(
            userId,
            status
        );
    }

    @Override
    protected List<ReputationRequestListResponse> fetch(
        CursorPageRequest<CursorDto> pageRequest,
        AppActor actor,
        UserReputationRequestFilterDto filter,
        Long userId
    ) {
        ReputationRequestStatus status = (filter != null) ? filter.getStatus() : null;
        return reputationRequestQueryRepository.getReputationRequestsWithCursorByUser(
            pageRequest,
            userId,
            status
        );
    }

}
