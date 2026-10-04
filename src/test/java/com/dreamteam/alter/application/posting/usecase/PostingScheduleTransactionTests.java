package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.outbound.posting.persistence.PostingQueryRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.posting.persistence.PostingRepositoryImpl;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingScheduleCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DataJpaTest(showSql = false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({QueryDslConfig.class, PostingQueryRepositoryImpl.class, PostingRepositoryImpl.class,
    CreatePosting.class, ManagerUpdatePosting.class})
class PostingScheduleTransactionTests {
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired CreatePosting createPosting;
    @Autowired ManagerUpdatePosting updatePosting;
    @MockitoBean WorkspaceQueryRepository workspaceQueryRepository;

    record Fixture(Workspace workspace, ManagerActor actor, Long postingId, List<Long> scheduleIds) {}

    @ParameterizedTest
    @ValueSource(strings = {"00:00", "09:30"})
    void create_invalidSecondSchedulePersistsNothing(String value) {
        Fixture fixture = fixture(false);
        LocalTime time = LocalTime.parse(value);
        when(workspaceQueryRepository.findByIdAndManagerUser(fixture.workspace().getId(), fixture.actor().getManagerUser()))
            .thenReturn(Optional.of(fixture.workspace()));

        assertThatThrownBy(() -> createPosting.execute(createCommand(fixture.workspace(),
            List.of(schedule("09:00", "18:00"), schedule(time, time))), fixture.actor()))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        tx().executeWithoutResult(status -> assertThat(em.createQuery(
            "select count(p) from Posting p where p.workspace.id = :id", Long.class)
            .setParameter("id", fixture.workspace().getId()).getSingleResult()).isZero());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:00", "09:30"})
    void update_invalidSecondScheduleRollsBackFirstScheduleAndContent(String value) {
        Fixture fixture = fixture(true);
        LocalTime time = LocalTime.parse(value);
        UpdatePostingCommand command = updateCommand(List.of(), List.of(
            updateSchedule(fixture.scheduleIds().get(0), LocalTime.of(10, 0), LocalTime.of(19, 0)),
            updateSchedule(fixture.scheduleIds().get(1), time, time)));

        assertThatThrownBy(() -> updatePosting.execute(fixture.postingId(), command, fixture.actor()))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertOriginalPosting(fixture);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:00", "09:30"})
    void add_invalidSecondScheduleRollsBackFirstAdditionAndContent(String value) {
        Fixture fixture = fixture(true);
        LocalTime time = LocalTime.parse(value);

        assertThatThrownBy(() -> updatePosting.execute(fixture.postingId(), updateCommand(
            List.of(schedule("10:00", "19:00"), schedule(time, time)), List.of()), fixture.actor()))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertOriginalPosting(fixture);
    }

    @Test
    void create_validDaytimeAndOvernightSchedulesPersistTogether() {
        Fixture fixture = fixture(false);
        when(workspaceQueryRepository.findByIdAndManagerUser(fixture.workspace().getId(), fixture.actor().getManagerUser()))
            .thenReturn(Optional.of(fixture.workspace()));
        createPosting.execute(createCommand(fixture.workspace(),
            List.of(schedule("09:00", "18:00"), schedule("22:00", "06:00"))), fixture.actor());

        tx().executeWithoutResult(status -> {
            Posting posting = em.createQuery("select p from Posting p where p.workspace.id = :id", Posting.class)
                .setParameter("id", fixture.workspace().getId()).getSingleResult();
            assertThat(posting.getSchedules()).hasSize(2);
            assertThat(posting.getSchedules()).extracting("startTime")
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(22, 0));
            assertThat(posting.getSchedules()).extracting("endTime")
                .containsExactly(LocalTime.of(18, 0), LocalTime.of(6, 0));
        });
    }

    @Test
    void updateAndAdd_validDaytimeAndOvernightSchedulesPersistTogether() {
        Fixture fixture = fixture(true);
        updatePosting.execute(fixture.postingId(), updateCommand(List.of(schedule("10:00", "19:00")),
            List.of(updateSchedule(fixture.scheduleIds().getFirst(), LocalTime.of(22, 0), LocalTime.of(6, 0)))),
            fixture.actor());

        tx().executeWithoutResult(status -> {
            Posting posting = em.find(Posting.class, fixture.postingId());
            assertThat(posting.getTitle()).isEqualTo("수정");
            assertThat(posting.getSchedules()).hasSize(3);
            assertThat(posting.getSchedules()).extracting("startTime")
                .containsExactlyInAnyOrder(LocalTime.of(22, 0), LocalTime.of(22, 0), LocalTime.of(10, 0));
        });
    }

    private void assertOriginalPosting(Fixture fixture) {
        tx().executeWithoutResult(status -> {
            Posting posting = em.find(Posting.class, fixture.postingId());
            assertThat(posting.getTitle()).isEqualTo("원본");
            assertThat(posting.getSchedules()).hasSize(2);
            assertThat(posting.getSchedules()).extracting("startTime")
                .containsExactlyInAnyOrder(LocalTime.of(9, 0), LocalTime.of(22, 0));
            assertThat(posting.getSchedules()).extracting("endTime")
                .containsExactlyInAnyOrder(LocalTime.of(18, 0), LocalTime.of(6, 0));
        });
    }

    private Fixture fixture(boolean withPosting) {
        return tx().execute(status -> {
            String unique = String.valueOf(System.nanoTime());
            User user = User.create("010" + unique.substring(0, 8), "encoded", "사장", "manager" + unique,
                UserGender.GENDER_MALE, "19990101", unique + "@example.com");
            em.persist(user);
            ManagerUser manager = ManagerUser.create(user, ManagerUserStatus.ACTIVATED);
            em.persist(manager);
            BusinessType businessType = BusinessType.create("업종" + unique, null);
            em.persist(businessType);
            Workspace workspace = Workspace.create(manager, "000-00-00000", "가게", businessType, null,
                "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울시 강남구", "서울특별시", "강남구", "역삼동",
                BigDecimal.ONE, BigDecimal.ONE);
            em.persist(workspace);
            Posting posting = null;
            if (withPosting) {
                posting = Posting.create(createCommand(workspace,
                    List.of(schedule("09:00", "18:00"), schedule("22:00", "06:00"))), workspace);
                em.persist(posting);
                em.flush();
            }
            return new Fixture(workspace, ManagerActor.from(manager, List.of()),
                posting == null ? null : posting.getId(), posting == null ? List.of() :
                posting.getSchedules().stream().map(s -> s.getId()).toList());
        });
    }

    private CreatePostingCommand createCommand(Workspace workspace, List<PostingScheduleCommand> schedules) {
        return new CreatePostingCommand(workspace.getId(), "원본", "설명", 12000, 3, PaymentType.HOURLY, schedules);
    }

    private UpdatePostingCommand updateCommand(List<PostingScheduleCommand> added, List<UpdatePostingScheduleCommand> updated) {
        return new UpdatePostingCommand("수정", "새 설명", 13000, 3, PaymentType.HOURLY, added, updated, List.of());
    }

    private PostingScheduleCommand schedule(String start, String end) {
        return schedule(LocalTime.parse(start), LocalTime.parse(end));
    }

    private PostingScheduleCommand schedule(LocalTime start, LocalTime end) {
        return new PostingScheduleCommand(List.of(DayOfWeek.MONDAY), start, end, "홀");
    }

    private UpdatePostingScheduleCommand updateSchedule(Long id, LocalTime start, LocalTime end) {
        return new UpdatePostingScheduleCommand(id, List.of(DayOfWeek.FRIDAY), start, end, "주방");
    }

    private TransactionTemplate tx() {
        return new TransactionTemplate(transactionManager);
    }
}
