package com.dreamteam.alter.adapter.outbound.admin.persistence;

import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardQueryRepository;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.report.entity.QReport;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceWorker;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class AdminDashboardQueryRepositoryImpl implements AdminDashboardQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QWorkspace workspace = QWorkspace.workspace;
    private final QUser user = QUser.user;
    private final QReport report = QReport.report;
    private final QWorkspaceWorker workspaceWorker = QWorkspaceWorker.workspaceWorker;

    @Override
    public List<PeriodCount> countWorkspacesByPeriod(DashboardPeriod period, int year) {
        NumberTemplate<Double> extractExpr = buildExtractExpr(period, workspace.createdAt);
        StringTemplate labelExpr = buildLabelExpr(period, workspace.createdAt);

        List<Tuple> results = queryFactory
            .select(labelExpr, workspace.count())
            .from(workspace)
            .where(
                workspace.status.eq(WorkspaceStatus.ACTIVATED),
                workspace.createdAt.year().eq(year)
            )
            .groupBy(extractExpr)
            .orderBy(extractExpr.asc())
            .fetch();

        return results.stream()
            .map(t -> new PeriodCount(
                t.get(labelExpr),
                Objects.requireNonNullElse(t.get(workspace.count()), 0L)
            ))
            .toList();
    }

    @Override
    public List<PeriodCount> countUsersByPeriod(DashboardPeriod period, int year) {
        NumberTemplate<Double> extractExpr = buildExtractExpr(period, user.createdAt);
        StringTemplate labelExpr = buildLabelExpr(period, user.createdAt);

        List<Tuple> results = queryFactory
            .select(labelExpr, user.count())
            .from(user)
            .where(
                user.createdAt.year().eq(year)
            )
            .groupBy(extractExpr)
            .orderBy(extractExpr.asc())
            .fetch();

        return results.stream()
            .map(t -> new PeriodCount(
                t.get(labelExpr),
                Objects.requireNonNullElse(t.get(user.count()), 0L)
            ))
            .toList();
    }

    @Override
    public long countReportsBetween(LocalDateTime from, LocalDateTime to) {
        Long count = queryFactory
            .select(report.count())
            .from(report)
            .where(
                report.createdAt.goe(from),
                report.createdAt.loe(to)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countActiveUsersBetween(LocalDateTime from, LocalDateTime to) {
        Long count = queryFactory
            .select(workspaceWorker.user.id.countDistinct())
            .from(workspaceWorker)
            .where(
                workspaceWorker.status.eq(WorkspaceWorkerStatus.ACTIVATED),
                workspaceWorker.createdAt.goe(from),
                workspaceWorker.createdAt.loe(to)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countWorkspacesInYear(int year) {
        Long count = queryFactory
            .select(workspace.count())
            .from(workspace)
            .where(
                workspace.status.eq(WorkspaceStatus.ACTIVATED),
                workspace.createdAt.year().eq(year)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countUsersInYear(int year) {
        Long count = queryFactory
            .select(user.count())
            .from(user)
            .where(
                user.createdAt.year().eq(year)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    private NumberTemplate<Double> buildExtractExpr(DashboardPeriod period, DateTimePath<LocalDateTime> dateField) {
        return switch (period) {
            case WEEKLY -> Expressions.numberTemplate(Double.class, "EXTRACT(WEEK FROM {0})", dateField);
            case MONTHLY -> Expressions.numberTemplate(Double.class, "EXTRACT(MONTH FROM {0})", dateField);
            case YEARLY -> Expressions.numberTemplate(Double.class, "EXTRACT(YEAR FROM {0})", dateField);
        };
    }

    private StringTemplate buildLabelExpr(DashboardPeriod period, DateTimePath<LocalDateTime> dateField) {
        return switch (period) {
            case WEEKLY -> Expressions.stringTemplate("CONCAT(EXTRACT(WEEK FROM {0}), '주')", dateField);
            case MONTHLY -> Expressions.stringTemplate("CONCAT(EXTRACT(MONTH FROM {0}), '월')", dateField);
            case YEARLY -> Expressions.stringTemplate("CAST(EXTRACT(YEAR FROM {0}) AS TEXT)", dateField);
        };
    }
}
