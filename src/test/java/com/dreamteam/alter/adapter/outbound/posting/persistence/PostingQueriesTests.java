package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CoordinateDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserPostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.PostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingScheduleCommand;
import com.dreamteam.alter.domain.posting.entity.*;
import com.dreamteam.alter.domain.posting.type.*;
import com.dreamteam.alter.domain.user.entity.*;
import com.dreamteam.alter.domain.user.type.*;
import com.dreamteam.alter.domain.workspace.entity.*;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:alter_posting_tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.dreamteam.alter.adapter.outbound.posting.persistence.PostingQueriesTests$SqlCapture"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QueryDslConfig.class, PostingQueryRepositoryImpl.class, PostingApplicationQueryRepositoryImpl.class})
public class PostingQueriesTests {
    @Autowired EntityManager em;
    @Autowired PostingQueryRepositoryImpl postings;
    @Autowired PostingApplicationQueryRepositoryImpl applications;
    private User user;
    private ManagerUser manager;
    private Workspace workspace;

    public static class SqlCapture implements StatementInspector {
        static final List<String> statements = new ArrayList<>();
        @Override public String inspect(String sql) { statements.add(sql); return sql; }
    }

    @BeforeEach
    void setup() {
        user = User.create("01000000000", "encoded", "알터", "user" + System.nanoTime(),
            UserGender.GENDER_MALE, "19990101", "user" + System.nanoTime() + "@example.com");
        em.persist(user);
        manager = ManagerUser.create(user, ManagerUserStatus.ACTIVATED);
        em.persist(manager);
        BusinessType business = BusinessType.create("업종" + System.nanoTime(), "설명");
        em.persist(business);
        workspace = Workspace.create(manager, "1234567890", "알터 카페", business, null,
            "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울", "서울", "강남구", "역삼동",
            new BigDecimal("37.500000"), new BigDecimal("127.000000"));
        em.persist(workspace);
    }

    private PostingScheduleCommand schedule(List<DayOfWeek> days, int start, int end) {
        return new PostingScheduleCommand(days, LocalTime.of(start, 0), LocalTime.of(end, 0), "홀서빙");
    }

    private Posting savePosting(PostingScheduleCommand... schedules) {
        Posting posting = Posting.create(new CreatePostingCommand(workspace.getId(), "공고", "설명",
            12000, 2, PaymentType.HOURLY, List.of(schedules)), workspace);
        em.persist(posting);
        return posting;
    }

    private void clear() { em.flush(); em.clear(); SqlCapture.statements.clear(); }

    @Test
    void weekdaysAndTimesMustMatchSameNonDeletedSchedule() {
        Posting split = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18),
            schedule(List.of(DayOfWeek.FRIDAY), 14, 20));
        Posting matching = savePosting(schedule(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), 14, 20),
            schedule(List.of(DayOfWeek.MONDAY), 15, 19));
        Posting deletedMatch = savePosting(schedule(List.of(DayOfWeek.MONDAY), 14, 20),
            schedule(List.of(DayOfWeek.TUESDAY), 14, 20));
        deletedMatch.getSchedules().getFirst().updateStatus(PostingStatus.DELETED);
        clear();
        PostingListFilterDto filter = new PostingListFilterDto();
        filter.setWorkingDays(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        filter.setStartTime(LocalTime.of(13, 0));
        filter.setEndTime(LocalTime.of(21, 0));

        assertThat(postings.getCountOfPostings(filter)).isEqualTo(1);
        List<PostingListResponse> result = postings.getPostingsWithCursor(CursorPageRequest.of(null, 10), filter, user);
        assertThat(result).extracting(PostingListResponse::getId).containsExactly(matching.getId());
        assertThat(SqlCapture.statements.stream().filter(sql -> sql.contains("exists"))).isNotEmpty();
        assertThat(result).extracting(PostingListResponse::getId).doesNotContain(split.getId(), deletedMatch.getId());
    }

    @Test
    void emptyWeekdaysKeepTimeFilterAndPagingHasNoDuplicatePostings() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), 14, 20),
            schedule(List.of(DayOfWeek.MONDAY), 15, 19));
        clear();
        PostingListFilterDto filter = new PostingListFilterDto();
        filter.setWorkingDays(List.of());
        filter.setStartTime(LocalTime.of(13, 0));
        assertThat(postings.getCountOfPostings(filter)).isEqualTo(1);
        List<PostingListResponse> first = postings.getPostingsWithCursor(CursorPageRequest.of(null, 1), filter, user);
        assertThat(first).hasSize(1);
        PostingListResponse last = first.getLast();
        assertThat(postings.getPostingsWithCursor(CursorPageRequest.of(new CursorDto(last.getId(), last.getCreatedAt()), 1), filter, user)).isEmpty();
        filter.setStartTime(LocalTime.of(16, 0));
        assertThat(postings.getCountOfPostings(filter)).isZero();
    }

    @Test
    void schedulesInitializeDaysWithOneAdditionalQuery() {
        savePosting(schedule(List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY), 9, 18),
            schedule(List.of(DayOfWeek.TUESDAY), 10, 19));
        savePosting(schedule(List.of(DayOfWeek.SUNDAY), 9, 18));
        clear();
        List<PostingListResponse> result = postings.getPostingsWithCursor(CursorPageRequest.of(null, 10), new PostingListFilterDto(), user);
        assertThat(result).hasSize(2);
        result.stream().flatMap(row -> row.getSchedules().stream()).forEach(schedule ->
            assertThat(Hibernate.isInitialized(ReflectionTestUtils.getField(schedule, "workingDays"))).isTrue());
        assertThat(SqlCapture.statements.stream().filter(sql -> sql.contains("posting_schedule_working_days")).count()).isEqualTo(1);
    }

    @Test
    void pendingQueryFetchesOnlyReviewingApplicationsAndTheirUsers() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18));
        for (PostingApplicationStatus status : PostingApplicationStatus.values()) {
            PostingApplication application = PostingApplication.create(posting.getSchedules().getFirst(), user, "지원");
            application.updateStatus(status);
            em.persist(application);
        }
        clear();
        List<PostingApplication> result = applications.findPendingByPostingIdWithUser(posting.getId());
        assertThat(result).extracting(PostingApplication::getStatus)
            .containsExactlyInAnyOrder(PostingApplicationStatus.SUBMITTED, PostingApplicationStatus.SHORTLISTED);
        result.forEach(application -> assertThat(Hibernate.isInitialized(application.getUser())).isTrue());
        assertThat(SqlCapture.statements).hasSize(1);
    }

    @Test
    void managedDaysUpdatePersistsAndReloadsSorted() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18));
        Long scheduleId = posting.getSchedules().getFirst().getId();
        clear();
        Posting managed = em.find(Posting.class, posting.getId());
        managed.updateContent(new UpdatePostingCommand("공고", "설명", 12000, 4, PaymentType.HOURLY,
            List.of(), List.of(new UpdatePostingScheduleCommand(scheduleId,
                List.of(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY), LocalTime.of(10, 0), LocalTime.of(19, 0), "주방")), List.of()));
        clear();
        assertThat(em.find(PostingSchedule.class, scheduleId).getWorkingDays()).containsExactly(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY);
        assertThat(em.find(Posting.class, posting.getId()).getRecruitCount()).isEqualTo(4);
    }

    private void assertDaysLoaded(PostingSchedule schedule) {
        assertThat(Hibernate.isInitialized(ReflectionTestUtils.getField(schedule, "workingDays"))).isTrue();
    }

    @Test
    void mapWorkspaceAndDetailQueriesInitializeDays() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18));
        clear();
        PostingMapListFilterDto mapFilter = new PostingMapListFilterDto(
            new CoordinateDto(new BigDecimal("37.0"), new BigDecimal("126.0")),
            new CoordinateDto(new BigDecimal("38.0"), new BigDecimal("128.0")), null, PostingSortType.LATEST);
        assertDaysLoaded(postings.getPostingMapListWithCursor(CursorPageRequest.of(null, 10), mapFilter, user)
            .getFirst().getSchedules().getFirst());
        clear();
        assertDaysLoaded(postings.getWorkspacePostingList(workspace.getId(), user).getFirst().getSchedules().getFirst());
        clear();
        assertDaysLoaded(postings.getPostingDetail(posting.getId(), user).getSchedules().getFirst());
        clear();
        assertDaysLoaded(postings.getManagerPostingsWithCursor(CursorPageRequest.of(null, 10), manager,
            new ManagerPostingListFilterDto()).getFirst().getSchedules().getFirst());
        clear();
        assertDaysLoaded(postings.getManagerPostingDetail(posting.getId(), manager).orElseThrow().getSchedules().getFirst());
    }

    @Test
    void applicationQueriesInitializeDaysOfHistoricalSchedule() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY), 9, 18));
        PostingApplication application = PostingApplication.create(posting.getSchedules().getFirst(), user, "지원");
        em.persist(application);
        posting.getSchedules().getFirst().updateStatus(PostingStatus.DELETED);
        clear();
        var userRows = applications.getUserPostingApplicationListWithCursor(user, CursorPageRequest.of(null, 10),
            new UserPostingApplicationListFilterDto());
        assertDaysLoaded(userRows.getFirst().getPostingSchedule());
        assertThat(userRows.getFirst().getPostingSchedule().getWorkingDays()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        clear();
        var managerRows = applications.getManagerPostingApplicationListWithCursor(manager, CursorPageRequest.of(null, 10),
            new PostingApplicationListFilterDto());
        assertDaysLoaded(managerRows.getFirst().getSchedule());
        clear();
        assertDaysLoaded(applications.getManagerPostingApplicationDetail(manager, application.getId()).orElseThrow().getSchedule());
    }

    @Test
    void acceptedCountsAreGroupedAndIncludeNoOtherStatus() {
        Posting first = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18));
        Posting second = savePosting(schedule(List.of(DayOfWeek.FRIDAY), 9, 18));
        for (Posting posting : List.of(first, second)) {
            for (PostingApplicationStatus status : PostingApplicationStatus.values()) {
                PostingApplication application = PostingApplication.create(posting.getSchedules().getFirst(), user, "지원");
                application.updateStatus(status);
                em.persist(application);
            }
        }
        clear();
        assertThat(applications.countAcceptedByPostingIds(List.of(first.getId(), second.getId())))
            .containsEntry(first.getId(), 1L).containsEntry(second.getId(), 1L).hasSize(2);
        assertThat(SqlCapture.statements).hasSize(1);
        assertThat(applications.countAcceptedByPostingIds(List.of())).isEmpty();
        assertThat(SqlCapture.statements).hasSize(1);
    }

    @Test
    void acceptedCountForSinglePostingIncludesOnlyAcceptedAndDefaultsToZero() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18));
        Posting emptyPosting = savePosting(schedule(List.of(DayOfWeek.FRIDAY), 9, 18));
        for (PostingApplicationStatus status : PostingApplicationStatus.values()) {
            PostingApplication application = PostingApplication.create(posting.getSchedules().getFirst(), user, "지원");
            application.updateStatus(status);
            em.persist(application);
        }
        clear();

        assertThat(applications.countAcceptedByPostingId(posting.getId())).isEqualTo(1L);
        assertThat(applications.countAcceptedByPostingId(emptyPosting.getId())).isZero();
        assertThat(applications.countAcceptedByPostingId(Long.MAX_VALUE)).isZero();
        assertThat(SqlCapture.statements).hasSize(3);
        assertThat(SqlCapture.statements).allMatch(sql -> !sql.contains("group by"));
    }

    @Test
    void scalarPostingLookupChecksUserOwnershipAndExcludesDeletedApplications() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18));
        PostingApplication application = PostingApplication.create(posting.getSchedules().getFirst(), user, "지원");
        em.persist(application);
        PostingApplication deleted = PostingApplication.create(posting.getSchedules().getFirst(), user, "삭제");
        deleted.updateStatus(PostingApplicationStatus.DELETED);
        em.persist(deleted);
        User otherUser = User.create("01000000001", "encoded", "다른 사용자", "other" + System.nanoTime(),
            UserGender.GENDER_MALE, "19990101", "other" + System.nanoTime() + "@example.com");
        em.persist(otherUser);
        clear();

        assertThat(applications.findPostingIdByUserAndApplicationId(user, application.getId()))
            .contains(posting.getId());
        assertThat(SqlCapture.statements).hasSize(1);
        assertThat(SqlCapture.statements.getFirst()).doesNotContain("for update");
        assertThat(SqlCapture.statements.getFirst()).contains("posting_id").doesNotContain("description");
        assertThat(applications.findPostingIdByUserAndApplicationId(otherUser, application.getId())).isEmpty();
        assertThat(applications.findPostingIdByUserAndApplicationId(user, deleted.getId())).isEmpty();
        assertThat(applications.findPostingIdByUserAndApplicationId(user, Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void scalarPostingLookupChecksManagerOwnershipAndExcludesDeletedApplications() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.MONDAY), 9, 18));
        PostingApplication application = PostingApplication.create(posting.getSchedules().getFirst(), user, "지원");
        em.persist(application);
        PostingApplication deleted = PostingApplication.create(posting.getSchedules().getFirst(), user, "삭제");
        deleted.updateStatus(PostingApplicationStatus.DELETED);
        em.persist(deleted);
        User otherUser = User.create("01000000001", "encoded", "다른 매니저", "other" + System.nanoTime(),
            UserGender.GENDER_MALE, "19990101", "other" + System.nanoTime() + "@example.com");
        em.persist(otherUser);
        ManagerUser otherManager = ManagerUser.create(otherUser, ManagerUserStatus.ACTIVATED);
        em.persist(otherManager);
        clear();

        assertThat(applications.findPostingIdByManagerAndApplicationId(manager, application.getId()))
            .contains(posting.getId());
        assertThat(SqlCapture.statements).hasSize(1);
        assertThat(SqlCapture.statements.getFirst()).doesNotContain("for update");
        assertThat(SqlCapture.statements.getFirst()).contains("posting_id").doesNotContain("description");
        assertThat(applications.findPostingIdByManagerAndApplicationId(otherManager, application.getId())).isEmpty();
        assertThat(applications.findPostingIdByManagerAndApplicationId(manager, deleted.getId())).isEmpty();
        assertThat(applications.findPostingIdByManagerAndApplicationId(manager, Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void applicationListsBatchDaysAcrossSharedAndEmptySchedules() {
        Posting posting = savePosting(schedule(List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY), 9, 18),
            schedule(List.of(DayOfWeek.SUNDAY), 10, 19), schedule(List.of(), 11, 20));
        for (PostingSchedule postingSchedule : posting.getSchedules()) {
            em.persist(PostingApplication.create(postingSchedule, user, "지원"));
        }
        em.persist(PostingApplication.create(posting.getSchedules().getFirst(), user, "중복 스케줄 지원"));
        clear();

        var userRows = applications.getUserPostingApplicationListWithCursor(user, CursorPageRequest.of(null, 10),
            new UserPostingApplicationListFilterDto());
        assertThat(userRows).hasSize(4);
        userRows.forEach(row -> assertDaysLoaded(row.getPostingSchedule()));
        assertThat(userRows).extracting(row -> row.getPostingSchedule().getWorkingDays())
            .containsExactlyInAnyOrder(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), List.of(DayOfWeek.SUNDAY), List.of());
        assertThat(SqlCapture.statements.stream().filter(sql -> sql.contains("posting_schedule_working_days")).count())
            .isEqualTo(1);
        clear();

        var managerRows = applications.getManagerPostingApplicationListWithCursor(manager, CursorPageRequest.of(null, 10),
            new PostingApplicationListFilterDto());
        assertThat(managerRows).hasSize(4);
        managerRows.forEach(row -> assertDaysLoaded(row.getSchedule()));
        assertThat(managerRows).extracting(row -> row.getSchedule().getWorkingDays())
            .containsExactlyInAnyOrder(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), List.of(DayOfWeek.SUNDAY), List.of());
        assertThat(SqlCapture.statements.stream().filter(sql -> sql.contains("posting_schedule_working_days")).count())
            .isEqualTo(1);
    }

    @Test
    void emptyQueryResultsSkipWorkingDayQueries() {
        clear();
        assertThat(postings.getPostingsWithCursor(CursorPageRequest.of(null, 10), new PostingListFilterDto(), user)).isEmpty();
        assertThat(postings.getWorkspacePostingList(workspace.getId(), user)).isEmpty();
        assertThat(applications.getUserPostingApplicationListWithCursor(user, CursorPageRequest.of(null, 10),
            new UserPostingApplicationListFilterDto())).isEmpty();
        assertThat(applications.getManagerPostingApplicationListWithCursor(manager, CursorPageRequest.of(null, 10),
            new PostingApplicationListFilterDto())).isEmpty();
        assertThat(applications.getManagerPostingApplicationDetail(manager, Long.MAX_VALUE)).isEmpty();
        assertThat(SqlCapture.statements).noneMatch(sql -> sql.contains("posting_schedule_working_days"));
    }
}
