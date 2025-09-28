package com.dreamteam.alter.adapter.outbound.report.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.report.dto.ReportListFilterDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportDetailResponse;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportListResponse;
import com.dreamteam.alter.domain.report.entity.Report;
import com.dreamteam.alter.domain.report.port.outbound.ReportQueryRepository;
import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import com.dreamteam.alter.domain.report.type.ReporterType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.dreamteam.alter.domain.report.entity.QReport.report;
import static com.dreamteam.alter.domain.user.entity.QUser.user;
import static com.dreamteam.alter.domain.workspace.entity.QWorkspace.workspace;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepositoryImpl implements ReportQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Report> findById(Long id) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(report)
                .where(report.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public Optional<ReportDetailResponse> findByIdWithTarget(Long id) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(
                    ReportDetailResponse.class,
                    report.id,
                    report.targetType,
                    report.targetId,
                    getTargetName(),
                    report.reason,
                    report.status,
                    report.adminComment,
                    report.createdAt,
                    report.updatedAt
                ))
                .from(report)
                .leftJoin(user)
                .on(
                    report.targetType.eq(ReportTargetType.USER)
                        .and(report.targetId.eq(user.id))
                )
                .leftJoin(workspace)
                .on(
                    (
                        report.targetType.eq(ReportTargetType.POSTING)
                            .and(report.targetId.eq(workspace.id))
                    ).or(
                        report.targetType.eq(ReportTargetType.WORKSPACE)
                            .and(report.targetId.eq(workspace.id))
                    )
                )
                .where(report.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public Optional<Report> findByIdAndReporter(Long id, ReporterType reporterType, Long reporterId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(report)
                .where(
                    report.id.eq(id),
                    report.reporterType.eq(reporterType),
                    report.reporterId.eq(reporterId),
                    report.status.ne(ReportStatus.DELETED)
                )
                .fetchOne()
        );
    }

    @Override
    public long getCountOfReports(ReportListFilterDto filter, ReporterType reporterType, Long reporterId) {
        return queryFactory
            .selectFrom(report)
            .where(
                report.reporterType.eq(reporterType),
                report.reporterId.eq(reporterId),
                report.status.ne(ReportStatus.DELETED),
                eqReportType(filter.getTargetType()),
                eqStatus(filter.getStatus())
            )
            .fetch()
            .size();
    }

    @Override
    public List<ReportListResponse> getReportsWithCursor(
        CursorPageRequest<CursorDto> pageRequest,
        ReportListFilterDto filter,
        ReporterType reporterType,
        Long reporterId
    ) {
        return queryFactory
            .select(Projections.constructor(
                ReportListResponse.class,
                report.id,
                report.targetType,
                getTargetName(),
                report.status,
                report.createdAt
            ))
            .from(report)
            .leftJoin(user)
            .on(
                report.targetType.eq(ReportTargetType.USER)
                    .and(report.targetId.eq(user.id))
            )
            .leftJoin(workspace)
            .on(
                (
                    report.targetType.eq(ReportTargetType.POSTING)
                        .and(report.targetId.eq(workspace.id))
                ).or(
                    report.targetType.eq(ReportTargetType.WORKSPACE)
                        .and(report.targetId.eq(workspace.id))
                )
            )
            .where(
                report.reporterType.eq(reporterType),
                report.reporterId.eq(reporterId),
                report.status.ne(ReportStatus.DELETED),
                eqReportType(filter.getTargetType()),
                eqStatus(filter.getStatus()),
                cursorCondition(pageRequest.cursor())
            )
            .orderBy(report.createdAt.desc(), report.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public long getAdminCountOfReports(ReportListFilterDto filter) {
        return queryFactory
            .selectFrom(report)
            .where(
                report.status.ne(ReportStatus.DELETED),
                eqReportType(filter.getTargetType()),
                eqStatus(filter.getStatus())
            )
            .fetch()
            .size();
    }

    @Override
    public List<ReportListResponse> getAdminReportsWithCursor(
        CursorPageRequest<CursorDto> pageRequest,
        ReportListFilterDto filter
    ) {
        return queryFactory
            .select(Projections.constructor(
                ReportListResponse.class,
                report.id,
                report.targetType,
                getTargetName(),
                report.status,
                report.createdAt
            ))
            .from(report)
            .leftJoin(user)
            .on(
                report.targetType.eq(ReportTargetType.USER)
                    .and(report.targetId.eq(user.id))
            )
            .leftJoin(workspace)
            .on(
                (
                    report.targetType.eq(ReportTargetType.POSTING)
                        .and(report.targetId.eq(workspace.id))
                ).or(
                    report.targetType.eq(ReportTargetType.WORKSPACE)
                        .and(report.targetId.eq(workspace.id))
                )
            )
            .where(
                report.status.ne(ReportStatus.DELETED),
                eqReportType(filter.getTargetType()),
                eqStatus(filter.getStatus()),
                cursorCondition(pageRequest.cursor())
            )
            .orderBy(report.createdAt.desc(), report.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    private BooleanExpression eqReportType(ReportTargetType reportType) {
        return ObjectUtils.isNotEmpty(reportType) ? report.targetType.eq(reportType) : null;
    }

    private BooleanExpression eqStatus(ReportStatus status) {
        return ObjectUtils.isNotEmpty(status) ? report.status.eq(status) : null;
    }

    private BooleanExpression cursorCondition(CursorDto cursor) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }
        return report.createdAt.lt(cursor.getCreatedAt())
            .or(report.createdAt.eq(cursor.getCreatedAt())
                .and(report.id.lt(cursor.getId())));
    }

    private SimpleExpression<String> getTargetName() {
        return new CaseBuilder()
            .when(report.targetType.eq(ReportTargetType.USER)).then(user.name)
            .when(report.targetType.eq(ReportTargetType.POSTING)).then(workspace.businessName)
            .when(report.targetType.eq(ReportTargetType.WORKSPACE)).then(workspace.businessName)
            .when(report.targetType.eq(ReportTargetType.REPUTATION)).then(Expressions.constant(""))
            .otherwise("알 수 없음");
    }
}
