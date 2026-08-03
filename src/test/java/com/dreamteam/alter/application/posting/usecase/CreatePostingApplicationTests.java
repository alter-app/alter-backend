package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingApplicationRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingScheduleQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CreatePostingApplication - 공고 지원 생성")
class CreatePostingApplicationTests {

    private static final Long POSTING_ID = 1L;
    private static final Long POSTING_SCHEDULE_ID = 10L;

    @Mock
    private PostingScheduleQueryRepository postingScheduleQueryRepository;

    @Mock
    private PostingQueryRepository postingQueryRepository;

    @Mock
    private PostingApplicationRepository postingApplicationRepository;

    @Mock
    private PostingApplicationQueryRepository postingApplicationQueryRepository;

    @Mock
    private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreatePostingApplication createPostingApplication;

    private AppActor givenActor(User user) {
        AppActor actor = mock(AppActor.class);
        given(actor.getUser()).willReturn(user);
        return actor;
    }

    private PostingSchedule givenOpenSchedule() {
        User managerOwner = mock(User.class);
        given(managerOwner.getId()).willReturn(99L);

        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(managerOwner);

        Workspace workspace = mock(Workspace.class);
        given(workspace.getManagerUser()).willReturn(managerUser);

        Posting posting = mock(Posting.class);
        given(posting.getId()).willReturn(POSTING_ID);
        given(posting.getTitle()).willReturn("홀서빙 구합니다");
        given(posting.getStatus()).willReturn(PostingStatus.OPEN);
        given(posting.getWorkspace()).willReturn(workspace);

        PostingSchedule schedule = mock(PostingSchedule.class);
        given(schedule.getPosting()).willReturn(posting);

        given(postingQueryRepository.findByIdWithPessimisticLock(POSTING_ID))
            .willReturn(Optional.of(posting));
        given(postingScheduleQueryRepository.findByIdAndPostingId(POSTING_ID, POSTING_SCHEDULE_ID))
            .willReturn(Optional.of(schedule));

        return schedule;
    }

    private CreatePostingApplicationRequestDto givenRequest() {
        CreatePostingApplicationRequestDto request = mock(CreatePostingApplicationRequestDto.class);
        given(request.getPostingScheduleId()).willReturn(POSTING_SCHEDULE_ID);
        given(request.getDescription()).willReturn("열심히 하겠습니다");
        return request;
    }

    @Nested
    @DisplayName("execute - 공고 조회 및 잠금")
    class PostingLookupTests {

        @Test
        @DisplayName("공고가 없으면 POSTING_NOT_FOUND 예외가 발생하고 저장하지 않는다")
        void throwsWhenPostingNotFound() {
            // given
            User user = mock(User.class);
            AppActor actor = givenActor(user);
            given(postingQueryRepository.findByIdWithPessimisticLock(POSTING_ID))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> createPostingApplication.execute(actor, POSTING_ID, givenRequest()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POSTING_NOT_FOUND);
            then(postingApplicationRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("공고 잠금 획득에 실패하면 TOO_MANY_REQUESTS 예외가 발생한다")
        void throwsWhenPostingLockAcquisitionFails() {
            // given
            // lock_timeout 초과는 @Repository 예외 변환을 거쳐 PessimisticLockingFailureException 으로 올라온다
            User user = mock(User.class);
            AppActor actor = givenActor(user);
            given(postingQueryRepository.findByIdWithPessimisticLock(POSTING_ID))
                .willThrow(new PessimisticLockingFailureException("lock timeout"));

            // when & then
            assertThatThrownBy(() -> createPostingApplication.execute(actor, POSTING_ID, givenRequest()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
            then(postingApplicationRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("모집이 종료된 공고면 중복 검사 전에 예외가 발생한다")
        void throwsWhenPostingIsNotOpen() {
            // given
            User user = mock(User.class);
            AppActor actor = givenActor(user);

            Posting posting = mock(Posting.class);
            given(posting.getStatus()).willReturn(PostingStatus.CLOSED);

            PostingSchedule schedule = mock(PostingSchedule.class);
            given(schedule.getPosting()).willReturn(posting);
            given(postingQueryRepository.findByIdWithPessimisticLock(POSTING_ID))
                .willReturn(Optional.of(posting));
            given(postingScheduleQueryRepository.findByIdAndPostingId(POSTING_ID, POSTING_SCHEDULE_ID))
                .willReturn(Optional.of(schedule));

            // when & then
            assertThatThrownBy(() -> createPostingApplication.execute(actor, POSTING_ID, givenRequest()))
                .isInstanceOf(CustomException.class);
            then(postingApplicationRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("execute - 중복 지원 차단")
    class DuplicateApplicationTests {

        @Test
        @DisplayName("같은 공고에 유효한 지원이 이미 있으면 예외가 발생하고 저장하지 않는다")
        void throwsWhenAlreadyApplied() {
            // given
            User user = mock(User.class);
            AppActor actor = givenActor(user);
            givenOpenSchedule();

            given(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(any(), any()))
                .willReturn(Optional.empty());
            given(postingApplicationQueryRepository.existsActiveByPostingIdAndUser(POSTING_ID, user))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> createPostingApplication.execute(actor, POSTING_ID, givenRequest()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT);
            then(postingApplicationRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("유효한 지원이 없으면 (취소·불합격 이력만 있는 경우 포함) 정상적으로 지원이 생성된다")
        void createsWhenNoActiveApplication() {
            // given
            User user = mock(User.class);
            AppActor actor = givenActor(user);
            givenOpenSchedule();

            given(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(any(), any()))
                .willReturn(Optional.empty());
            given(postingApplicationQueryRepository.existsActiveByPostingIdAndUser(POSTING_ID, user))
                .willReturn(false);

            // when
            createPostingApplication.execute(actor, POSTING_ID, givenRequest());

            // then
            then(postingApplicationRepository).should().save(any(PostingApplication.class));
            then(eventPublisher).should().publishEvent(any(FcmNotificationEvent.class));
        }

        @Test
        @DisplayName("중복 검사는 공고 스케줄이 아니라 공고 단위로 수행한다")
        void checksDuplicationByPostingNotBySchedule() {
            // given
            User user = mock(User.class);
            AppActor actor = givenActor(user);
            givenOpenSchedule();

            given(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(any(), any()))
                .willReturn(Optional.empty());
            given(postingApplicationQueryRepository.existsActiveByPostingIdAndUser(POSTING_ID, user))
                .willReturn(false);

            // when
            createPostingApplication.execute(actor, POSTING_ID, givenRequest());

            // then
            // 스케줄 ID가 아닌 공고 ID로 조회되어야 다른 스케줄 중복 지원까지 막힌다
            then(postingApplicationQueryRepository).should().existsActiveByPostingIdAndUser(POSTING_ID, user);
        }
    }
}
