package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetReceivedSubstituteRequestsFilterDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ReceivedSubstituteRequestListResponse;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    QueryDslConfig.class,
    SubstituteRequestQueryRepositoryImpl.class,
    SubstituteRequestRepositoryImpl.class,
    WorkspaceShiftRepositoryImpl.class,
    WorkspaceWorkerRepositoryImpl.class,
    WorkspaceRepositoryImpl.class,
    UserRepositoryImpl.class,
    ManagerUserRepositoryImpl.class,
    BusinessTypeRepositoryImpl.class
})
class SubstituteRequestQueryRepositoryImplReceivedTests {

    @Autowired
    private SubstituteRequestQueryRepositoryImpl substituteRequestQueryRepository;

    @Autowired
    private SubstituteRequestRepositoryImpl substituteRequestRepository;

    @Autowired
    private WorkspaceShiftRepositoryImpl workspaceShiftRepository;

    @Autowired
    private WorkspaceWorkerRepositoryImpl workspaceWorkerRepository;

    @Autowired
    private WorkspaceRepositoryImpl workspaceRepository;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private ManagerUserRepositoryImpl managerUserRepository;

    @Autowired
    private BusinessTypeRepositoryImpl businessTypeRepository;

    private static final AtomicInteger SEQ = new AtomicInteger();

    private Workspace workspace;
    private WorkspaceWorker requester;
    private User receiverUser;

    private User saveUser() {
        User user = User.create(
            String.format("010%08d", SEQ.incrementAndGet()), "encoded", "김알바",
            "nickname" + System.nanoTime(), UserGender.GENDER_MALE, "19990101",
            "user" + System.nanoTime() + "@example.com"
        );
        return userRepository.save(user);
    }

    private WorkspaceWorker saveWorker(User user) {
        WorkspaceWorker worker = WorkspaceWorker.create(workspace, user);
        workspaceWorkerRepository.save(worker);
        return worker;
    }

    @BeforeEach
    void setUp() {
        ManagerUser managerUser = managerUserRepository.save(ManagerUser.create(saveUser(), ManagerUserStatus.ACTIVATED));
        BusinessType businessType = businessTypeRepository.save(BusinessType.create("업종" + System.nanoTime(), null));
        workspace = Workspace.create(
            managerUser, "000-00-00000", "사장님가게", businessType, null,
            "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울시 강남구",
            "서울특별시", "강남구", "역삼동", BigDecimal.ONE, BigDecimal.ONE
        );
        workspaceRepository.save(workspace);

        requester = saveWorker(saveUser());
        receiverUser = saveUser();
        saveWorker(receiverUser);
    }

    private SubstituteRequest saveRequest(Consumer<SubstituteRequest> transition) {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        WorkspaceShift shift = WorkspaceShift.create(workspace, start, start.plusHours(4), "홀", WorkspaceShiftStatus.CONFIRMED);
        shift.assignWorker(requester);
        workspaceShiftRepository.save(shift);

        SubstituteRequest request = SubstituteRequest.create(shift, requester.getId(), SubstituteRequestType.ALL, "사정");
        transition.accept(request);
        substituteRequestRepository.save(request);
        return request;
    }

    private List<Long> receivedIds(SubstituteRequestStatus status) {
        GetReceivedSubstituteRequestsFilterDto filter = new GetReceivedSubstituteRequestsFilterDto(null, status);
        CursorPageRequest<CursorDto> page = CursorPageRequest.of(null, 20);
        return substituteRequestQueryRepository.getReceivedRequestListWithCursor(receiverUser, filter, page).stream()
            .map(ReceivedSubstituteRequestListResponse::getId)
            .toList();
    }

    private long receivedCount(SubstituteRequestStatus status) {
        return substituteRequestQueryRepository.getReceivedRequestCount(
            receiverUser, new GetReceivedSubstituteRequestsFilterDto(null, status));
    }

    @Test
    void 받은_요청_상태_미지정시_취소_만료_요청_제외() {
        SubstituteRequest pending = saveRequest(r -> {});
        saveRequest(SubstituteRequest::cancel);
        saveRequest(SubstituteRequest::expire);

        assertThat(receivedIds(null)).containsExactly(pending.getId());
        assertThat(receivedCount(null)).isEqualTo(1);
    }

    @Test
    void 받은_요청_CANCELLED_명시하면_조회() {
        saveRequest(r -> {});
        SubstituteRequest cancelled = saveRequest(SubstituteRequest::cancel);

        assertThat(receivedIds(SubstituteRequestStatus.CANCELLED)).containsExactly(cancelled.getId());
        assertThat(receivedCount(SubstituteRequestStatus.CANCELLED)).isEqualTo(1);
    }
}
