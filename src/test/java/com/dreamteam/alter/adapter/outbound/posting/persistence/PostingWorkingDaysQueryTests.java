package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserPostingApplicationListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.UserPostingApplicationListResponse;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.QPosting;
import com.dreamteam.alter.domain.posting.entity.QPostingApplication;
import com.dreamteam.alter.domain.user.entity.User;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PostingWorkingDaysQueryTests {

    @Mock JPAQueryFactory queryFactory;
    @Mock EntityManager entityManager;
    @InjectMocks PostingQueryRepositoryImpl postings;
    @InjectMocks PostingApplicationQueryRepositoryImpl applications;

    @Test
    void singlePostingAcceptedCountReturnsTheCountQueryResult() {
        @SuppressWarnings("unchecked")
        JPAQuery<Long> countQuery = mock(JPAQuery.class, RETURNS_SELF);
        given(queryFactory.select(QPostingApplication.postingApplication.count())).willReturn(countQuery);
        given(countQuery.fetchOne()).willReturn(3L);

        assertThat(applications.countAcceptedByPostingId(1L)).isEqualTo(3L);
    }

    @Test
    void singlePostingAcceptedCountDefaultsToZeroWhenTheCountQueryReturnsNull() {
        @SuppressWarnings("unchecked")
        JPAQuery<Long> countQuery = mock(JPAQuery.class, RETURNS_SELF);
        given(queryFactory.select(QPostingApplication.postingApplication.count())).willReturn(countQuery);

        assertThat(applications.countAcceptedByPostingId(1L)).isZero();
        then(countQuery).should().fetchOne();
    }

    @Test
    void emptyWorkspacePostingListNeedsOnlyThePostingQuery() {
        @SuppressWarnings("unchecked")
        JPAQuery<Posting> postingQuery = mock(JPAQuery.class, RETURNS_SELF);
        given(queryFactory.selectFrom(QPosting.posting)).willReturn(postingQuery);
        given(postingQuery.fetch()).willReturn(List.of());

        assertThat(postings.getWorkspacePostingList(1L, mock(User.class))).isEmpty();

        then(queryFactory).should().selectFrom(QPosting.posting);
        then(queryFactory).shouldHaveNoMoreInteractions();
    }

    @Test
    void emptyUserApplicationListNeedsOnlyTheApplicationQuery() {
        @SuppressWarnings("unchecked")
        JPAQuery<UserPostingApplicationListResponse> applicationQuery = mock(JPAQuery.class, RETURNS_SELF);
        given(queryFactory.select(org.mockito.ArgumentMatchers.<Expression<UserPostingApplicationListResponse>>any()))
            .willReturn(applicationQuery);
        given(applicationQuery.fetch()).willReturn(List.of());

        assertThat(applications.getUserPostingApplicationListWithCursor(mock(User.class),
            CursorPageRequest.of(null, 10), new UserPostingApplicationListFilterDto())).isEmpty();

        then(queryFactory).should().select(any(Expression.class));
        then(queryFactory).shouldHaveNoMoreInteractions();
    }
}
