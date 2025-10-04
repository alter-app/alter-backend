package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserReputationRequestFilterDto;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.domain.reputation.port.inbound.ManagerGetSentReputationRequestListUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("managerGetSentReputationRequestList")
public class ManagerGetSentReputationRequestList extends AbstractGetSentReputationRequestList<ManagerActor, UserReputationRequestFilterDto, Long>
    implements ManagerGetSentReputationRequestListUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;

    public ManagerGetSentReputationRequestList(ReputationRequestQueryRepository reputationRequestQueryRepository, ObjectMapper objectMapper) {
        super(objectMapper);
        this.reputationRequestQueryRepository = reputationRequestQueryRepository;
    }

    @Override
    protected Long prepare(ManagerActor actor, UserReputationRequestFilterDto filter) {
        return actor.getManagerUser().getId();
    }

    @Override
    protected void validate(ManagerActor actor, UserReputationRequestFilterDto filter, Long managerId) { }

    @Override
    protected long count(ManagerActor actor, UserReputationRequestFilterDto filter, Long managerId) {
        ReputationRequestStatus status = (filter != null) ? filter.getStatus() : null;
        return reputationRequestQueryRepository.getCountOfSentReputationRequestsByManager(
            managerId,
            status
        );
    }

    @Override
    protected List<ReputationRequestListResponse> fetch(
        CursorPageRequest<CursorDto> pageRequest,
        ManagerActor actor,
        UserReputationRequestFilterDto filter,
        Long managerId
    ) {
        ReputationRequestStatus status = (filter != null) ? filter.getStatus() : null;
        return reputationRequestQueryRepository.getSentReputationRequestsWithCursorByManager(
            pageRequest,
            managerId,
            status
        );
    }

}
