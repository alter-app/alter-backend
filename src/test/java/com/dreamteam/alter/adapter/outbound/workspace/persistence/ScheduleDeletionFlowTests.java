package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.schedule.dto.ApproveSubstituteRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.application.workspace.usecase.GetMySchedule;
import com.dreamteam.alter.application.workspace.usecase.ManagerApproveSubstituteRequest;
import com.dreamteam.alter.application.workspace.usecase.ManagerDeleteWorkSchedule;
import com.dreamteam.alter.application.workspace.usecase.ManagerAssignWorkerToSchedule;
import com.dreamteam.alter.application.workspace.usecase.ManagerUpdateWorkerInSchedule;
import com.dreamteam.alter.application.workspace.usecase.ManagerRemoveWorkerFromSchedule;
import com.dreamteam.alter.application.workspace.usecase.GetWorkspaceWorkSchedule;
import com.dreamteam.alter.common.constants.WorkspaceConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequestTarget;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({GetMySchedule.class, ManagerDeleteWorkSchedule.class, ManagerApproveSubstituteRequest.class,
    ManagerAssignWorkerToSchedule.class, ManagerUpdateWorkerInSchedule.class, ManagerRemoveWorkerFromSchedule.class,
    GetWorkspaceWorkSchedule.class,
    SubstituteRequestQueryRepositoryImpl.class, WorkspaceWorkerQueryRepositoryImpl.class})
class ScheduleDeletionFlowTests extends WorkspaceShiftPersistenceTestSupport {
    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 7, 9, 0);
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired GetMySchedule getMine;
    @Autowired ManagerDeleteWorkSchedule delete;
    @Autowired ManagerApproveSubstituteRequest approve;
    @Autowired ManagerAssignWorkerToSchedule assign;
    @Autowired ManagerUpdateWorkerInSchedule replace;
    @Autowired ManagerRemoveWorkerFromSchedule remove;
    @Autowired GetWorkspaceWorkSchedule getWorkspace;
    @MockitoBean NotificationService notifications;

    record Fixture(Long managerId, Long userId, Long shiftId, Long requestId, Long targetId) {}

    @ParameterizedTest
    @EnumSource(value = WorkspaceShiftStatus.class, names = {"PLANNED", "CONFIRMED", "CANCELLED"})
    void dayQueryPreservesNonDeletedStateMeaning(WorkspaceShiftStatus state) {
        Long userId = tx().execute(status -> {
            WorkspaceWorker worker = saveWorker(saveWorkspace(), saveUser());
            WorkspaceShift shift = saveConfirmedShift(worker, START, START.plusHours(3));
            em.createQuery("update WorkspaceShift s set s.status = :state where s.id = :id")
                .setParameter("state", state).setParameter("id", shift.getId()).executeUpdate();
            return worker.getUser().getId();
        });
        tx().executeWithoutResult(status -> assertThat(workspaceShiftQueryRepository.findByUserAndDate(
            em.find(User.class, userId), 2030, 1, 7)).extracting(WorkspaceShift::getStatus).containsExactly(state));
    }

    @Test
    void monthWeeklyAndWorkspaceSalaryContractsRemainUnchanged() {
        Long userId = tx().execute(status -> {
            Workspace workspace = saveWorkspace();
            WorkspaceWorker worker = saveWorker(workspace, saveUser());
            saveConfirmedShift(worker, START, START.plusHours(3));
            saveConfirmedShift(worker, START.plusDays(1), START.plusDays(1).plusHours(3));
            saveConfirmedShift(saveWorker(workspace, saveUser()), START.plusHours(4), START.plusHours(6));
            WorkspaceShift deleted = saveConfirmedShift(worker, START.plusHours(8), START.plusHours(9));
            deleted.delete();
            return worker.getUser().getId();
        });
        tx().executeWithoutResult(status -> {
            User user = em.find(User.class, userId);
            var month = getMine.execute(AppActor.from(user, List.of()), new WorkScheduleInquiryRequestDto(2030, 1, null));
            assertThat(month.getSchedules()).hasSize(2);
            assertThat(month.getTotalWorkHours()).isEqualTo(6.0);
            assertThat(month.getEstimatedSalary()).isEqualTo(6L * WorkspaceConstants.MINIMUM_HOURLY_WAGE);
            var weekly = workspaceShiftQueryRepository.findByUserAndWeeklyRange(user, START.toLocalDate(), START.toLocalDate().plusDays(2));
            assertThat(weekly).hasSize(2).allSatisfy(shift -> assertThat(shift.getStatus()).isEqualTo(WorkspaceShiftStatus.CONFIRMED));
            Workspace workspace = weekly.getFirst().getWorkspace();
            var firstDay = getWorkspace.execute(AppActor.from(user, List.of()), workspace.getId(), new WorkScheduleInquiryRequestDto(2030, 1, 7));
            var secondDay = getWorkspace.execute(AppActor.from(user, List.of()), workspace.getId(), new WorkScheduleInquiryRequestDto(2030, 1, 8));
            assertThat(firstDay.getTotalWorkHours()).isEqualTo(6.0);
            assertThat(firstDay.getEstimatedSalary()).isEqualTo(6L * WorkspaceConstants.MINIMUM_HOURLY_WAGE);
            assertThat(firstDay.getSchedules()).extracting("shiftId")
                .containsExactlyElementsOf(secondDay.getSchedules().stream().map(schedule -> schedule.getShiftId()).toList());
            assertThat(workspaceShiftQueryRepository.findByManagerAndDateRange(workspace.getManagerUser(), workspace.getId(), 2030, 1))
                .hasSize(3);
        });
    }

    @Test
    void normalAssignmentReplacementReleaseAndDeletePreserveNotificationsAndRetryBehavior() {
        record Flow(Long managerId, Long shiftId, Long firstWorker, Long secondWorker) {}
        Flow fixture = tx().execute(status -> {
            Workspace workspace = saveWorkspace();
            WorkspaceWorker first = saveWorker(workspace, saveUser());
            WorkspaceWorker second = saveWorker(workspace, saveUser());
            WorkspaceShift shift = WorkspaceShift.create(workspace, START, START.plusHours(3), "홀", WorkspaceShiftStatus.PLANNED);
            workspaceShiftRepository.save(shift);
            return new Flow(workspace.getManagerUser().getId(), shift.getId(), first.getId(), second.getId());
        });
        tx().executeWithoutResult(status -> assign.execute(ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of()),
            fixture.shiftId(), fixture.firstWorker()));
        tx().executeWithoutResult(status -> replace.execute(ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of()),
            fixture.shiftId(), fixture.secondWorker()));
        tx().executeWithoutResult(status -> assertThat(em.find(WorkspaceShift.class, fixture.shiftId()).getAssignedWorkspaceWorker().getId())
            .isEqualTo(fixture.secondWorker()));
        tx().executeWithoutResult(status -> remove.execute(ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of()), fixture.shiftId()));
        verify(notifications, times(2)).sendNotification(any());
        clearInvocations(notifications);
        tx().executeWithoutResult(status -> {
            WorkspaceShift shift = em.find(WorkspaceShift.class, fixture.shiftId());
            assertThat(shift.getStatus()).isEqualTo(WorkspaceShiftStatus.CANCELLED);
            assertThat(shift.getAssignedWorkspaceWorker()).isNull();
        });
        tx().executeWithoutResult(status -> delete.execute(ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of()), fixture.shiftId()));
        tx().executeWithoutResult(status -> assertThat(em.find(WorkspaceShift.class, fixture.shiftId()).getStatus()).isEqualTo(WorkspaceShiftStatus.DELETED));
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> delete.execute(
            ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of()), fixture.shiftId())))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
        verifyNoInteractions(notifications);
    }

    @Test
    void deletedAssignedShiftIsAbsentFromDayListAndWorkHours() {
        Long userId = tx().execute(status -> {
            Workspace workspace = saveWorkspace();
            User user = saveUser();
            WorkspaceWorker worker = saveWorker(workspace, user);
            saveConfirmedShift(worker, START, START.plusHours(3));
            WorkspaceShift removed = saveConfirmedShift(worker, START.plusHours(4), START.plusHours(6));
            delete.execute(ManagerActor.from(workspace.getManagerUser(), List.of()), removed.getId());
            return user.getId();
        });
        tx().executeWithoutResult(status -> {
            var result = getMine.execute(AppActor.from(em.find(User.class, userId), List.of()),
                new WorkScheduleInquiryRequestDto(2030, 1, 7));
            assertThat(result.getSchedules()).hasSize(1);
            assertThat(result.getTotalWorkHours()).isEqualTo(3.0);
            assertThat(result.getEstimatedSalary()).isNull();
        });
        verifyNoInteractions(notifications);
    }

    @Test
    void approvingAcceptedRequestCannotReviveDeletedOriginalOrChangeRequest() {
        Fixture fixture = tx().execute(status -> {
            Workspace workspace = saveWorkspace();
            WorkspaceWorker requester = saveWorker(workspace, saveUser());
            WorkspaceWorker target = saveWorker(workspace, saveUser());
            WorkspaceShift shift = saveConfirmedShift(requester, START, START.plusHours(3));
            SubstituteRequest request = SubstituteRequest.create(shift, requester.getId(), SubstituteRequestType.SPECIFIC, "교환");
            SubstituteRequestTarget recipient = SubstituteRequestTarget.create(request, target.getId());
            request.getTargets().add(recipient);
            request.accept(target.getId());
            em.persist(request);
            em.flush();
            return new Fixture(workspace.getManagerUser().getId(), target.getUser().getId(), shift.getId(), request.getId(), recipient.getId());
        });
        tx().executeWithoutResult(status -> delete.execute(actor(fixture), fixture.shiftId()));

        Throwable failure = catchThrowable(() -> tx().executeWithoutResult(status ->
            approve.execute(actor(fixture), fixture.requestId(), new ApproveSubstituteRequestDto("승인"))));

        tx().executeWithoutResult(status -> {
            assertThat(em.find(WorkspaceShift.class, fixture.shiftId()).getStatus()).isEqualTo(WorkspaceShiftStatus.DELETED);
            assertThat(em.find(SubstituteRequest.class, fixture.requestId()).getStatus()).isEqualTo(SubstituteRequestStatus.ACCEPTED);
            assertThat(em.find(SubstituteRequestTarget.class, fixture.targetId()).getStatus()).isEqualTo(SubstituteRequestTargetStatus.ACCEPTED);
        });
        assertThat(failure).isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);
        verifyNoInteractions(notifications);
    }

    private ManagerActor actor(Fixture fixture) {
        return ManagerActor.from(em.find(ManagerUser.class, fixture.managerId()), List.of());
    }

    private TransactionTemplate tx() { return new TransactionTemplate(transactionManager); }
}
