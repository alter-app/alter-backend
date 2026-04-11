package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UnlinkSocialAccountRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialRepository;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnlinkSocialAccount 테스트")
class UnlinkSocialAccountTests {

    @Mock
    private UserSocialQueryRepository userSocialQueryRepository;

    @Mock
    private UserSocialRepository userSocialRepository;

    @InjectMocks
    private UnlinkSocialAccount unlinkSocialAccount;

    private AppActor actor;
    private User user;
    private UserSocial userSocial;
    private UnlinkSocialAccountRequestDto request;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        actor = mock(AppActor.class);
        userSocial = mock(UserSocial.class);
        request = new UnlinkSocialAccountRequestDto(SocialProvider.KAKAO);

        given(actor.getUser()).willReturn(user);
        given(user.getId()).willReturn(1L);
    }

    @Test
    @DisplayName("연동되지 않은 provider 해제 시 SOCIAL_ACCOUNT_NOT_LINKED 예외 발생")
    void execute_withNotLinkedProvider_throwsException() {
        // given
        given(userSocialQueryRepository.findByUserIdAndSocialProvider(1L, SocialProvider.KAKAO))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> unlinkSocialAccount.execute(actor, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SOCIAL_ACCOUNT_NOT_LINKED);
        then(userSocialRepository).shouldHaveNoInteractions();
    }

    @Nested
    @DisplayName("비밀번호가 있는 사용자")
    class UserWithPassword {

        @BeforeEach
        void setUp() {
            given(user.getPassword()).willReturn("encodedPassword");
            given(userSocialQueryRepository.findByUserIdAndSocialProvider(1L, SocialProvider.KAKAO))
                .willReturn(Optional.of(userSocial));
        }

        @Test
        @DisplayName("연동된 소셜 계정을 정상 해제한다")
        void execute_withLinkedSocial_succeeds() {
            // when & then
            assertThatNoException().isThrownBy(() -> unlinkSocialAccount.execute(actor, request));
            then(userSocialRepository).should().delete(userSocial);
        }

        @Test
        @DisplayName("소셜 계정이 1개뿐이어도 해제 가능하다 (제약은 비밀번호 없는 사용자에만 적용)")
        void execute_withSingleSocial_succeeds() {
            // when & then
            assertThatNoException().isThrownBy(() -> unlinkSocialAccount.execute(actor, request));
            then(userSocialRepository).should().delete(userSocial);
        }
    }

    @Nested
    @DisplayName("비밀번호가 없는 소셜 전용 사용자")
    class SocialOnlyUser {

        @BeforeEach
        void setUp() {
            given(user.getPassword()).willReturn(null);
            given(userSocialQueryRepository.findByUserIdAndSocialProvider(1L, SocialProvider.KAKAO))
                .willReturn(Optional.of(userSocial));
        }

        @Test
        @DisplayName("소셜 계정이 2개일 때 1개 해제 성공")
        void execute_withMultipleSocials_succeeds() {
            // given
            given(userSocialQueryRepository.countByUserId(1L)).willReturn(2L);

            // when & then
            assertThatNoException().isThrownBy(() -> unlinkSocialAccount.execute(actor, request));
            then(userSocialRepository).should().delete(userSocial);
        }

        @Test
        @DisplayName("마지막 소셜 계정(1개) 해제 시도 시 SOCIAL_UNLINK_NOT_ALLOWED 예외 발생")
        void execute_withSingleSocial_throwsException() {
            // given
            given(userSocialQueryRepository.countByUserId(1L)).willReturn(1L);

            // when & then
            assertThatThrownBy(() -> unlinkSocialAccount.execute(actor, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SOCIAL_UNLINK_NOT_ALLOWED);
            then(userSocialRepository).shouldHaveNoInteractions();
        }
    }
}
