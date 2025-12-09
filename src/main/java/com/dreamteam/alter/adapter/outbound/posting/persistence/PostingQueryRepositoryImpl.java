package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CoordinateDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapListFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapMarkerFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.*;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.domain.posting.entity.*;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingSortType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.entity.QUserFavoritePosting;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostingQueryRepositoryImpl implements PostingQueryRepository {

    private static final int MARKER_MAX_COUNT = 50;

    private final JPAQueryFactory queryFactory;

    @Override
    public long getCountOfPostings(PostingListFilterDto filter) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qPosting.countDistinct())
            .from(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule)
            .leftJoin(qPosting.workspace, qWorkspace)
            .where(
                qPosting.status.eq(PostingStatus.OPEN),
                eqProvince(qWorkspace, filter.getProvince()),
                eqDistrict(qWorkspace, filter.getDistrict()),
                eqTown(qWorkspace, filter.getTown()),
                gtePayAmount(qPosting, filter.getMinPayAmount()),
                ltePayAmount(qPosting, filter.getMaxPayAmount()),
                gteStartTime(qPostingSchedule, filter.getStartTime()),
                lteEndTime(qPostingSchedule, filter.getEndTime())
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public long getCountOfPostingMapList(PostingMapListFilterDto filter) {
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qPosting.countDistinct())
            .from(qPosting)
            .leftJoin(qPosting.workspace, qWorkspace)
            .where(
                qPosting.status.eq(PostingStatus.OPEN),
                withinBounds(qWorkspace, filter.getCoordinate1(), filter.getCoordinate2()),
                containsSearchKeyword(qPosting, qWorkspace, filter.getSearchKeyword())
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<PostingListResponse> getPostingsWithCursor(CursorPageRequest<CursorDto> request, PostingListFilterDto filter, User user) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUserFavoritePosting qUserFavoritePosting = QUserFavoritePosting.userFavoritePosting;

        List<Long> postingIds = queryFactory
            .select(qPosting.id)
            .from(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule)
            .leftJoin(qPosting.workspace, qWorkspace)
            .where(
                qPosting.status.eq(PostingStatus.OPEN),
                cursorConditions(qPosting, request.cursor(), filter.getPayAmountSort()),
                eqProvince(qWorkspace, filter.getProvince()),
                eqDistrict(qWorkspace, filter.getDistrict()),
                eqTown(qWorkspace, filter.getTown()),
                gtePayAmount(qPosting, filter.getMinPayAmount()),
                ltePayAmount(qPosting, filter.getMaxPayAmount()),
                gteStartTime(qPostingSchedule, filter.getStartTime()),
                lteEndTime(qPostingSchedule, filter.getEndTime())
            )
            .groupBy(qPosting.id, qPosting.payAmount, qPosting.createdAt)
            .orderBy(getOrderSpecifiers(qPosting, filter.getPayAmountSort()))
            .limit(request.pageSize())
            .fetch();

        if (ObjectUtils.isEmpty(postingIds)) {
            return Collections.emptyList();
        }

        List<Posting> postings = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .leftJoin(qPosting.workspace, qWorkspace).fetchJoin()
            .where(qPosting.id.in(postingIds))
            .orderBy(getOrderSpecifiers(qPosting, filter.getPayAmountSort()))
            .distinct()
            .fetch();

        Set<Long> scrappedPostingIds =
            new HashSet<>(queryFactory.select(qUserFavoritePosting.posting.id)
                .from(qUserFavoritePosting)
                .where(
                    qUserFavoritePosting.user.eq(user),
                    qUserFavoritePosting.posting.id.in(postingIds)
                )
                .fetch());

        List<PostingKeywordMap> keywordMaps = queryFactory
            .selectFrom(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword).fetchJoin()
            .where(qPostingKeywordMap.posting.id.in(postingIds))
            .fetch();

        Map<Long, List<PostingKeyword>> postingIdToKeywords = keywordMaps.stream()
            .collect(
                Collectors.groupingBy(
                    pkMap -> pkMap.getPosting().getId(),
                    Collectors.mapping(PostingKeywordMap::getPostingKeyword, Collectors.toList())
                )
            );

        return postings.stream()
            .map(
                posting -> PostingListResponse.of(
                    posting,
                    postingIdToKeywords,
                    scrappedPostingIds.contains(posting.getId())
                )
            )
            .toList();
    }

    @Override
    public List<PostingListResponse> getPostingMapListWithCursor(CursorPageRequest<CursorDto> request, PostingMapListFilterDto filter, User user) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUserFavoritePosting qUserFavoritePosting = QUserFavoritePosting.userFavoritePosting;

        // getPostingMapMarkers와 동일한 좌표 기준으로 공고 목록을 커서 페이징으로 조회
        List<Long> postingIds = queryFactory
            .select(qPosting.id)
            .from(qPosting)
            .leftJoin(qPosting.workspace, qWorkspace)
            .where(
                qPosting.status.eq(PostingStatus.OPEN),
                cursorConditionsForMapList(qPosting, request.cursor(), filter.getSortType()),
                withinBounds(qWorkspace, filter.getCoordinate1(), filter.getCoordinate2()),
                containsSearchKeyword(qPosting, qWorkspace, filter.getSearchKeyword())
            )
            .orderBy(getOrderSpecifiersForMapList(qPosting, filter.getSortType()))
            .limit(request.pageSize())
            .fetch();

        if (ObjectUtils.isEmpty(postingIds)) {
            return Collections.emptyList();
        }

        List<Posting> postings = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .leftJoin(qPosting.workspace, qWorkspace).fetchJoin()
            .where(qPosting.id.in(postingIds))
            .orderBy(getOrderSpecifiersForMapList(qPosting, filter.getSortType()))
            .distinct()
            .fetch();

        Set<Long> scrappedPostingIds =
            new HashSet<>(queryFactory.select(qUserFavoritePosting.posting.id)
                .from(qUserFavoritePosting)
                .where(
                    qUserFavoritePosting.user.eq(user),
                    qUserFavoritePosting.posting.id.in(postingIds)
                )
                .fetch());

        List<PostingKeywordMap> keywordMaps = queryFactory
            .selectFrom(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword).fetchJoin()
            .where(qPostingKeywordMap.posting.id.in(postingIds))
            .fetch();

        Map<Long, List<PostingKeyword>> postingIdToKeywords = keywordMaps.stream()
            .collect(
                Collectors.groupingBy(
                    pkMap -> pkMap.getPosting().getId(),
                    java.util.stream.Collectors.mapping(PostingKeywordMap::getPostingKeyword, java.util.stream.Collectors.toList())
                )
            );

        return postings.stream()
            .map(
                posting -> PostingListResponse.of(
                    posting,
                    postingIdToKeywords,
                    scrappedPostingIds.contains(posting.getId())
                )
            )
            .toList();
    }

    @Override
    public List<PostingListForMapMarkerResponse> getPostingListForMapMarker(PostingMapMarkerFilterDto filter) {
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;

        List<PostingListForMapMarkerResponse> results = queryFactory.select(
                Projections.constructor(
                    PostingListForMapMarkerResponse.class,
                    qWorkspace.id,
                    qWorkspace.latitude,
                    qWorkspace.longitude
                )

            )
            .from(qPosting)
            .leftJoin(qPosting.workspace, qWorkspace)
            .where(
                qPosting.status.eq(PostingStatus.OPEN),
                withinBounds(qWorkspace, filter.getCoordinate1(), filter.getCoordinate2())
            )
            .groupBy(qWorkspace.id, qWorkspace.businessName, qWorkspace.latitude, qWorkspace.longitude)
            .orderBy(qWorkspace.id.asc())
            .limit(MARKER_MAX_COUNT)
            .fetch();

        if (ObjectUtils.isEmpty(results)) {
            return Collections.emptyList();
        }

        return results;
    }

    @Override
    public List<PostingListResponse> getWorkspacePostingList(Long workspaceId, User user) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUserFavoritePosting qUserFavoritePosting = QUserFavoritePosting.userFavoritePosting;

        List<Posting> postings = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.workspace, qWorkspace).fetchJoin()
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .where(
                qPosting.status.eq(PostingStatus.OPEN),
                qWorkspace.id.eq(workspaceId)
            )
            .orderBy(qPosting.createdAt.desc(), qPosting.id.desc())
            .fetch();

        if (ObjectUtils.isEmpty(postings)) {
            return Collections.emptyList();
        }

        List<Long> postingIds = postings.stream()
            .map(Posting::getId)
            .toList();

        Set<Long> scrappedPostingIds = new HashSet<>(
            queryFactory.select(qUserFavoritePosting.posting.id)
                .from(qUserFavoritePosting)
                .where(
                    qUserFavoritePosting.user.eq(user),
                    qUserFavoritePosting.posting.id.in(postingIds)
                )
                .fetch()
        );

        List<PostingKeywordMap> keywordMaps = queryFactory
            .selectFrom(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword).fetchJoin()
            .where(qPostingKeywordMap.posting.id.in(postingIds))
            .fetch();

        Map<Long, List<PostingKeyword>> postingIdToKeywords = keywordMaps.stream()
            .collect(
                Collectors.groupingBy(
                    pkMap -> pkMap.getPosting().getId(),
                    Collectors.mapping(PostingKeywordMap::getPostingKeyword, Collectors.toList())
                )
            );

        return postings.stream()
            .map(posting -> PostingListResponse.of(
                posting,
                postingIdToKeywords,
                scrappedPostingIds.contains(posting.getId())
            ))
            .toList();
    }

    @Override
    public PostingDetailResponse getPostingDetail(Long postingId, User user) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QUserFavoritePosting qUserFavoritePosting = QUserFavoritePosting.userFavoritePosting;

        Posting posting = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .leftJoin(qPosting.workspace, qWorkspace).fetchJoin()
            .where(qPosting.id.eq(postingId))
            .fetchOne();

        if (ObjectUtils.isEmpty(posting)) {
            return null;
        }

        List<PostingKeyword> postingKeywords = queryFactory
            .select(qPostingKeyword)
            .from(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword)
            .where(qPostingKeywordMap.posting.id.eq(postingId))
            .fetch();

        boolean scrapped = ObjectUtils.isNotEmpty(
            queryFactory
                .selectOne()
                .from(qUserFavoritePosting)
                .where(
                    qUserFavoritePosting.user.eq(user),
                    qUserFavoritePosting.posting.id.eq(postingId)
                )
                .fetchFirst()
        );

        return PostingDetailResponse.of(posting, postingKeywords, scrapped);
    }

    @Override
    public Optional<Posting> findById(Long postingId) {
        QPosting qPosting = QPosting.posting;

        Posting posting = queryFactory
            .select(qPosting)
            .from(qPosting)
            .where(
                qPosting.id.eq(postingId),
                qPosting.status.eq(PostingStatus.OPEN)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(posting) ? Optional.empty() : Optional.of(posting);
    }

    @Override
    public long getManagerPostingCount(ManagerUser managerUser, ManagerPostingListFilterDto filter) {
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Long count = queryFactory
            .select(qPosting.count())
            .from(qPosting)
            .join(qPosting.workspace, qWorkspace)
            .where(
                qWorkspace.managerUser.eq(managerUser),
                eqWorkspaceId(qWorkspace, filter.getWorkspaceId()),
                eqPostingStatus(qPosting, filter.getStatus()),
                qPosting.status.ne(PostingStatus.DELETED)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    @Override
    public List<ManagerPostingListResponse> getManagerPostingsWithCursor(
        CursorPageRequest<CursorDto> request,
        ManagerUser managerUser,
        ManagerPostingListFilterDto filter
    ) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;
        QWorkspace qWorkspace = QWorkspace.workspace;

        List<Long> postingIds = queryFactory
            .select(qPosting.id)
            .from(qPosting)
            .join(qPosting.workspace, qWorkspace)
            .where(
                qWorkspace.managerUser.eq(managerUser),
                eqWorkspaceId(qWorkspace, filter.getWorkspaceId()),
                eqPostingStatus(qPosting, filter.getStatus()),
                cursorConditions(qPosting, request.cursor(), false),
                qPosting.status.ne(PostingStatus.DELETED)
            )
            .orderBy(qPosting.createdAt.desc(), qPosting.id.desc())
            .limit(request.pageSize())
            .fetch();

        if (ObjectUtils.isEmpty(postingIds)) {
            return Collections.emptyList();
        }

        List<Posting> postings = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .leftJoin(qPosting.workspace, qWorkspace).fetchJoin()
            .where(qPosting.id.in(postingIds))
            .orderBy(qPosting.createdAt.desc(), qPosting.id.desc())
            .distinct()
            .fetch();

        List<PostingKeywordMap> keywordMaps = queryFactory
            .selectFrom(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword).fetchJoin()
            .where(qPostingKeywordMap.posting.id.in(postingIds))
            .fetch();

        Map<Long, List<PostingKeyword>> postingIdToKeywords = keywordMaps.stream()
            .collect(
                Collectors.groupingBy(
                    pkMap -> pkMap.getPosting().getId(),
                    Collectors.mapping(PostingKeywordMap::getPostingKeyword, Collectors.toList())
                )
            );

        return postings.stream()
            .map(
                posting -> ManagerPostingListResponse.of(
                    posting,
                    postingIdToKeywords
                )
            )
            .toList();
    }

    private BooleanExpression cursorConditions(QPosting qPosting, CursorDto cursor, Boolean payAmountSort) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }
        
        // 급여순 정렬인 경우 급여와 ID 조건을 모두 적용
        if (BooleanUtils.isTrue(payAmountSort)) {
            return ltPayAmountAndId(qPosting, cursor);
        } else {
            // 최신순 정렬인 경우 생성일시와 ID 조건을 모두 적용
            return ltCreatedAtAndId(qPosting, cursor);
        }
    }

    private BooleanExpression ltCreatedAtAndId(QPosting qPosting, CursorDto cursor) {
        BooleanExpression createdAtCondition = cursor.getCreatedAt() != null 
            ? qPosting.createdAt.lt(cursor.getCreatedAt()) 
            : null;
        BooleanExpression idCondition = cursor.getId() != null 
            ? qPosting.id.lt(cursor.getId()) 
            : null;
        
        // 생성일시가 같을 때만 ID로 비교
        BooleanExpression createdAtEqualCondition = cursor.getCreatedAt() != null 
            ? qPosting.createdAt.eq(cursor.getCreatedAt()).and(qPosting.id.lt(cursor.getId()))
            : null;
        
        if (createdAtCondition != null && idCondition != null) {
            return createdAtCondition.or(createdAtEqualCondition);
        } else if (createdAtCondition != null) {
            return createdAtCondition;
        } else if (idCondition != null) {
            return idCondition;
        }
        return null;
    }

    private BooleanExpression ltPayAmountAndId(QPosting qPosting, CursorDto cursor) {
        if (cursor.getId() == null) {
            return null;
        }
        
        // 마지막 조회된 공고의 급여 정보를 조회
        Integer lastPayAmount = queryFactory
            .select(qPosting.payAmount)
            .from(qPosting)
            .where(qPosting.id.eq(cursor.getId()))
            .fetchOne();
        
        if (lastPayAmount == null) {
            // 해당 ID의 공고가 없으면 ID만으로 비교
            return qPosting.id.lt(cursor.getId());
        }
        
        // 급여가 다른 경우: payAmount < lastPayAmount
        BooleanExpression payAmountCondition = qPosting.payAmount.lt(lastPayAmount);
        
        // 급여가 같은 경우: payAmount = lastPayAmount AND id < cursor.id
        BooleanExpression payAmountEqualCondition = qPosting.payAmount.eq(lastPayAmount)
            .and(qPosting.id.lt(cursor.getId()));
        
        return payAmountCondition.or(payAmountEqualCondition);
    }

    private BooleanExpression eqWorkspaceId(QWorkspace qWorkspace, Long workspaceId) {
        return workspaceId != null ? qWorkspace.id.eq(workspaceId) : null;
    }

    private BooleanExpression eqPostingStatus(QPosting qPosting, PostingStatus status) {
        return status != null ? qPosting.status.eq(status) : null;
    }

    private BooleanExpression eqProvince(QWorkspace qWorkspace, String province) {
        return province != null ? qWorkspace.province.eq(province) : null;
    }

    private BooleanExpression eqDistrict(QWorkspace qWorkspace, String district) {
        return district != null ? qWorkspace.district.eq(district) : null;
    }

    private BooleanExpression eqTown(QWorkspace qWorkspace, String town) {
        return town != null ? qWorkspace.town.eq(town) : null;
    }

    private BooleanExpression gtePayAmount(QPosting qPosting, Integer minPayAmount) {
        return minPayAmount != null ? qPosting.payAmount.goe(minPayAmount) : null;
    }

    private BooleanExpression ltePayAmount(QPosting qPosting, Integer maxPayAmount) {
        return maxPayAmount != null ? qPosting.payAmount.loe(maxPayAmount) : null;
    }

    private BooleanExpression gteStartTime(QPostingSchedule qPostingSchedule, java.time.LocalTime startTime) {
        return startTime != null ? qPostingSchedule.startTime.goe(startTime) : null;
    }

    private BooleanExpression lteEndTime(QPostingSchedule qPostingSchedule, java.time.LocalTime endTime) {
        return endTime != null ? qPostingSchedule.endTime.loe(endTime) : null;
    }

    private BooleanExpression withinBounds(QWorkspace qWorkspace, CoordinateDto coordinate1, CoordinateDto coordinate2) {
        BigDecimal minLat = coordinate1.getLatitude().min(coordinate2.getLatitude());
        BigDecimal maxLat = coordinate1.getLatitude().max(coordinate2.getLatitude());
        BigDecimal minLng = coordinate1.getLongitude().min(coordinate2.getLongitude());
        BigDecimal maxLng = coordinate1.getLongitude().max(coordinate2.getLongitude());
        
        return qWorkspace.latitude.between(minLat, maxLat)
            .and(qWorkspace.longitude.between(minLng, maxLng));
    }

    private BooleanExpression containsSearchKeyword(QPosting qPosting, QWorkspace qWorkspace, String searchKeyword) {
        if (ObjectUtils.isEmpty(searchKeyword) || searchKeyword.isBlank()) {
            return null;
        }
        return qPosting.title.containsIgnoreCase(searchKeyword)
            .or(qWorkspace.businessName.containsIgnoreCase(searchKeyword));
    }

    private OrderSpecifier<?>[] getOrderSpecifiersForMapList(QPosting qPosting, PostingSortType sortType) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        
        switch (sortType) {
            case PAY_AMOUNT:
                orderSpecifiers.add(qPosting.payAmount.desc());
                orderSpecifiers.add(qPosting.id.desc());
                break;
            case LATEST:
                orderSpecifiers.add(qPosting.createdAt.desc());
                orderSpecifiers.add(qPosting.id.desc());
                break;
        }
        
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression cursorConditionsForMapList(QPosting qPosting, CursorDto cursor, PostingSortType sortType) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }

        return switch (sortType) {
            case PAY_AMOUNT -> ltPayAmountAndId(qPosting, cursor);
            case LATEST -> ltCreatedAtAndId(qPosting, cursor);
        };
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(QPosting qPosting, Boolean payAmountSort) {
        List<OrderSpecifier<?>> orderSpecifiers = new java.util.ArrayList<>();
        
        // 급여순이 true인 경우 급여를 첫 번째 정렬 기준으로 설정
        if (BooleanUtils.isTrue(payAmountSort)) {
            orderSpecifiers.add(qPosting.payAmount.desc());
            // 급여가 같은 경우 ID로 정렬
            orderSpecifiers.add(qPosting.id.desc());
        } else {
            // 기본적으로 최신순 정렬 적용
            orderSpecifiers.add(qPosting.createdAt.desc());
            // 마지막에 ID로 정렬하여 일관성 보장
            orderSpecifiers.add(qPosting.id.desc());
        }
        
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }


    @Override
    public Optional<ManagerPostingDetailResponse> getManagerPostingDetail(Long postingId, ManagerUser managerUser) {
        QPosting qPosting = QPosting.posting;
        QPostingSchedule qPostingSchedule = QPostingSchedule.postingSchedule;
        QPostingKeywordMap qPostingKeywordMap = QPostingKeywordMap.postingKeywordMap;
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Posting posting = queryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.schedules, qPostingSchedule).fetchJoin()
            .leftJoin(qPosting.workspace, qWorkspace).fetchJoin()
            .where(
                qPosting.id.eq(postingId),
                qWorkspace.managerUser.eq(managerUser)
            )
            .fetchOne();

        if (ObjectUtils.isEmpty(posting)) {
            return Optional.empty();
        }

        List<PostingKeyword> postingKeywords = queryFactory
            .select(qPostingKeyword)
            .from(qPostingKeywordMap)
            .leftJoin(qPostingKeywordMap.postingKeyword, qPostingKeyword)
            .where(qPostingKeywordMap.posting.id.eq(postingId))
            .fetch();

        return Optional.of(ManagerPostingDetailResponse.of(posting, postingKeywords));
    }

    @Override
    public Optional<Posting> findByManagerAndId(Long postingId, ManagerUser managerUser) {
        QPosting qPosting = QPosting.posting;
        QWorkspace qWorkspace = QWorkspace.workspace;

        Posting posting = queryFactory
            .selectFrom(qPosting)
            .join(qPosting.workspace, qWorkspace)
            .where(
                qPosting.id.eq(postingId),
                qWorkspace.managerUser.eq(managerUser)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(posting) ? Optional.empty() : Optional.of(posting);
    }


    @Override
    public PostingFilterOptionsResponse getPostingFilterOptions() {
        QWorkspace qWorkspace = QWorkspace.workspace;
        QPosting qPosting = QPosting.posting;

        List<String> provinces = queryFactory
            .select(qWorkspace.province)
            .from(qPosting)
            .join(qPosting.workspace, qWorkspace)
            .where(qPosting.status.eq(PostingStatus.OPEN))
            .groupBy(qWorkspace.province)
            .orderBy(qWorkspace.province.asc())
            .fetch();

        List<String> districts = queryFactory
            .select(qWorkspace.district)
            .from(qPosting)
            .join(qPosting.workspace, qWorkspace)
            .where(qPosting.status.eq(PostingStatus.OPEN))
            .groupBy(qWorkspace.district)
            .orderBy(qWorkspace.district.asc())
            .fetch();

        List<String> towns = queryFactory
            .select(qWorkspace.town)
            .from(qPosting)
            .join(qPosting.workspace, qWorkspace)
            .where(qPosting.status.eq(PostingStatus.OPEN))
            .groupBy(qWorkspace.town)
            .orderBy(qWorkspace.town.asc())
            .fetch();

        return PostingFilterOptionsResponse.of(provinces, districts, towns);
    }



}
