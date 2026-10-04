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
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequestTarget;
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
    private WorkspaceWorker receiver;
    private WorkspaceWorker other;

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
        receiver = saveWorker(receiverUser);
        other = saveWorker(saveUser());
    }

    /** requester가 보낸 요청에 targetWorkers를 대상자로 추가하고 transition을 적용해 저장 */
    private SubstituteRequest saveRequest(
        WorkspaceWorker requesterWorker,
        SubstituteRequestType type,
        List<WorkspaceWorker> targetWorkers,
        Consumer<SubstituteRequest> transition
    ) {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        WorkspaceShift shift = WorkspaceShift.create(workspace, start, start.plusHours(4), "홀", WorkspaceShiftStatus.CONFIRMED);
        shift.assignWorker(requesterWorker);
        workspaceShiftRepository.save(shift);

        SubstituteRequest request = SubstituteRequest.create(shift, requesterWorker.getId(), type, "사정");
        targetWorkers.forEach(w -> request.getTargets().add(SubstituteRequestTarget.create(request, w.getId())));
        transition.accept(request);
        substituteRequestRepository.save(request);
        return request;
    }

    /** requester가 receiver·other 전체에게 보낸 ALL 요청 */
    private SubstituteRequest saveAllRequest(Consumer<SubstituteRequest> transition) {
        return saveRequest(requester, SubstituteRequestType.ALL, List.of(receiver, other), transition);
    }

    private List<ReceivedSubstituteRequestListResponse> received(SubstituteRequestStatus status) {
        GetReceivedSubstituteRequestsFilterDto filter = new GetReceivedSubstituteRequestsFilterDto(null, status);
        CursorPageRequest<CursorDto> page = CursorPageRequest.of(null, 20);
        return substituteRequestQueryRepository.getReceivedRequestListWithCursor(receiverUser, filter, page);
    }

    private List<Long> receivedIds(SubstituteRequestStatus status) {
        return received(status).stream().map(ReceivedSubstituteRequestListResponse::getId).toList();
    }

    private long receivedCount(SubstituteRequestStatus status) {
        return substituteRequestQueryRepository.getReceivedRequestCount(
            receiverUser, new GetReceivedSubstituteRequestsFilterDto(null, status));
    }

    @Test
    void 받은_요청_상태_미지정시_본인_대상자_상태_기준으로_노출() {
        SubstituteRequest pending = saveAllRequest(r -> {});
        SubstituteRequest rejectedByMe = saveAllRequest(r -> r.rejectByTarget(receiver.getId(), "불가"));
        SubstituteRequest acceptedByMe = saveAllRequest(r -> r.accept(receiver.getId()));
        SubstituteRequest approvedForMe = saveAllRequest(r -> {
            r.accept(receiver.getId());
            r.approve(1L, "ok");
        });
        SubstituteRequest rejectedByApproverForMe = saveAllRequest(r -> {
            r.accept(receiver.getId());
            r.rejectByApprover(1L, "no");
        });
        SubstituteRequest allRejected = saveAllRequest(r -> {
            r.rejectByTarget(receiver.getId(), "불가");
            r.rejectByTarget(other.getId(), "불가");
        });
        SubstituteRequest cancelledAfterMyAccept = saveAllRequest(r -> {
            r.accept(receiver.getId());
            r.cancel();
        });
        saveAllRequest(r -> r.accept(other.getId()));
        saveAllRequest(r -> {
            r.accept(other.getId());
            r.approve(1L, "ok");
        });
        saveAllRequest(r -> {
            r.accept(other.getId());
            r.rejectByApprover(1L, "no");
        });
        saveAllRequest(r -> {
            r.rejectByTarget(receiver.getId(), "불가");
            r.cancelPendingTargetAndCancelIfNoPendingTargets(other.getId());
        });
        saveAllRequest(SubstituteRequest::cancel);
        saveAllRequest(SubstituteRequest::expire);

        List<ReceivedSubstituteRequestListResponse> result = received(null);

        assertThat(result).extracting(ReceivedSubstituteRequestListResponse::getId).containsExactlyInAnyOrder(
            pending.getId(), rejectedByMe.getId(), acceptedByMe.getId(), approvedForMe.getId(),
            rejectedByApproverForMe.getId(), allRejected.getId(), cancelledAfterMyAccept.getId()
        );
        assertThat(receivedCount(null)).isEqualTo(result.size());
    }

    @Test
    void 받은_요청_CANCELLED_명시하면_수락_여부_무관하게_취소_요청_조회() {
        saveAllRequest(r -> {});
        SubstituteRequest cancelled = saveAllRequest(SubstituteRequest::cancel);
        SubstituteRequest cancelledAfterMyAccept = saveAllRequest(r -> {
            r.accept(receiver.getId());
            r.cancel();
        });

        assertThat(receivedIds(SubstituteRequestStatus.CANCELLED))
            .containsExactlyInAnyOrder(cancelled.getId(), cancelledAfterMyAccept.getId());
        assertThat(receivedCount(SubstituteRequestStatus.CANCELLED)).isEqualTo(2);
    }

    @Test
    void 받은_요청_SPECIFIC은_본인이_대상자인_요청만_조회() {
        SubstituteRequest toMe = saveRequest(requester, SubstituteRequestType.SPECIFIC, List.of(receiver), r -> {});
        saveRequest(requester, SubstituteRequestType.SPECIFIC, List.of(other), r -> {});

        assertThat(receivedIds(null)).containsExactly(toMe.getId());
        assertThat(receivedCount(null)).isEqualTo(1);
    }

    @Test
    void 받은_요청_ALL이어도_본인_대상자_행이_없으면_미조회() {
        saveRequest(requester, SubstituteRequestType.ALL, List.of(other), r -> {});

        assertThat(receivedIds(null)).isEmpty();
        assertThat(receivedCount(null)).isEqualTo(0);
    }

    @Test
    void 받은_요청_재입사해도_예전_근무자_id로_보낸_본인_요청은_미조회() {
        receiver.resign();
        workspaceWorkerRepository.save(receiver);
        saveRequest(receiver, SubstituteRequestType.ALL, List.of(other), r -> r.rejectByTarget(other.getId(), "불가"));
        WorkspaceWorker rejoined = saveWorker(receiverUser);
        SubstituteRequest toRejoined = saveRequest(requester, SubstituteRequestType.ALL, List.of(rejoined), r -> {});

        assertThat(receivedIds(null)).containsExactly(toRejoined.getId());
        assertThat(receivedCount(null)).isEqualTo(1);
    }
}
