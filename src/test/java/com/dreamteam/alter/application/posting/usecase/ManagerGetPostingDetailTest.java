package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingDetailResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingDetailResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ManagerGetPostingDetail - 매니저 공고 상세 조회")
class ManagerGetPostingDetailTest {

    private static final Long POSTING_ID = 1L;

    @Mock
    private PostingQueryRepository postingQueryRepository;

    @Mock
    private PostingApplicationQueryRepository postingApplicationQueryRepository;

    @InjectMocks
    private ManagerGetPostingDetail managerGetPostingDetail;

    private ManagerActor givenActor() {
        ManagerActor actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(mock(ManagerUser.class));
        return actor;
    }

    private ManagerPostingDetailResponse buildPostingDetail() {
        BusinessType businessType = mock(BusinessType.class);
        given(businessType.getName()).willReturn("음식점");

        Workspace workspace = mock(Workspace.class);
        given(workspace.getId()).willReturn(5L);
        given(workspace.getBusinessName()).willReturn("알터 카페");
        given(workspace.getBusinessType()).willReturn(businessType);

        Posting posting = mock(Posting.class);
        given(posting.getId()).willReturn(POSTING_ID);
        given(posting.getWorkspace()).willReturn(workspace);
        given(posting.getTitle()).willReturn("홀서빙 구합니다");
        given(posting.getDescription()).willReturn("많은 지원 바랍니다");
        given(posting.getPayAmount()).willReturn(10000);
        given(posting.getPaymentType()).willReturn(PaymentType.HOURLY);
        given(posting.getStatus()).willReturn(PostingStatus.OPEN);
        given(posting.getCreatedAt()).willReturn(LocalDateTime.of(2026, 7, 1, 12, 0));
        given(posting.getUpdatedAt()).willReturn(LocalDateTime.of(2026, 7, 2, 12, 0));
        given(posting.getSchedules()).willReturn(Collections.emptyList());

        return ManagerPostingDetailResponse.of(posting);
    }

    @Test
    @DisplayName("지원자 수 조회 결과가 응답의 applicantCount 로 전달된다")
    void mapsApplicantCountIntoResponse() {
        ManagerActor actor = givenActor();
        ManagerPostingDetailResponse detail = buildPostingDetail();

        given(postingQueryRepository.getManagerPostingDetail(eq(POSTING_ID), any(ManagerUser.class)))
            .willReturn(Optional.of(detail));
        given(postingApplicationQueryRepository.countActiveApplicationsByPostingId(POSTING_ID))
            .willReturn(7L);

        ManagerPostingDetailResponseDto result = managerGetPostingDetail.execute(POSTING_ID, actor);

        assertThat(result.getApplicantCount()).isEqualTo(7L);
        assertThat(result.getId()).isEqualTo(POSTING_ID);
    }

    @Test
    @DisplayName("지원자가 없으면 applicantCount 는 0 이다")
    void returnsZeroWhenNoApplicant() {
        ManagerActor actor = givenActor();
        ManagerPostingDetailResponse detail = buildPostingDetail();

        given(postingQueryRepository.getManagerPostingDetail(eq(POSTING_ID), any(ManagerUser.class)))
            .willReturn(Optional.of(detail));
        given(postingApplicationQueryRepository.countActiveApplicationsByPostingId(POSTING_ID))
            .willReturn(0L);

        ManagerPostingDetailResponseDto result = managerGetPostingDetail.execute(POSTING_ID, actor);

        assertThat(result.getApplicantCount()).isZero();
    }

    @Test
    @DisplayName("공고가 없으면 POSTING_NOT_FOUND 예외가 발생하고 지원자 수는 조회하지 않는다")
    void throwsWhenPostingNotFound() {
        ManagerActor actor = givenActor();

        given(postingQueryRepository.getManagerPostingDetail(eq(POSTING_ID), any(ManagerUser.class)))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> managerGetPostingDetail.execute(POSTING_ID, actor))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_NOT_FOUND);

        verify(postingApplicationQueryRepository, never()).countActiveApplicationsByPostingId(POSTING_ID);
    }
}
