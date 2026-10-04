package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserWithSocialRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.adapter.outbound.auth.external.KakaoAuthClientImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.SignupSessionCacheRepository;
import com.dreamteam.alter.application.auth.manager.SocialAuthenticationManager;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.auth.service.KakaoSocialAuth;
import com.dreamteam.alter.application.terms.service.TermsAgreementValidator;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.user.command.LinkSocialAccountCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SocialTokenErrorPropagationTests {
    private SocialAuthenticationManager authenticationManager;
    private final UserSocialQueryRepository socialRepository = mock(UserSocialQueryRepository.class);

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "{\"error\":\"invalid_grant\",\"error_code\":\"KOE303\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8));
        authenticationManager = new SocialAuthenticationManager(
            List.of(new KakaoSocialAuth(new KakaoAuthClientImpl(new ObjectMapper(), restTemplate))));
    }

    @Test
    void loginStopsBeforeTokenIssuanceOnConfigurationError() {
        AuthService authService = mock(AuthService.class);
        AuthLogRepository authLogRepository = mock(AuthLogRepository.class);
        LoginWithSocial useCase = new LoginWithSocial(authenticationManager, socialRepository, authService, authLogRepository);
        assertConfigurationError(() -> useCase.execute(
            new SocialLoginRequestDto(SocialProvider.KAKAO, null, "synthetic-code", PlatformType.WEB)));
        verifyNoInteractions(socialRepository, authService, authLogRepository);
    }

    @Test
    void signupDoesNotPersistUserOrDeleteSessionOnConfigurationError() {
        UserQueryRepository userRepository = mock(UserQueryRepository.class);
        SignupSessionCacheRepository cache = mock(SignupSessionCacheRepository.class);
        CreateUserWithSocialTx tx = mock(CreateUserWithSocialTx.class);
        TermsAgreementValidator terms = mock(TermsAgreementValidator.class);
        when(cache.get(anyString())).thenReturn("synthetic-contact");
        CreateUserWithSocialRequestDto request = new CreateUserWithSocialRequestDto();
        ReflectionTestUtils.setField(request, "signupSessionId", "synthetic-session");
        ReflectionTestUtils.setField(request, "provider", SocialProvider.KAKAO);
        ReflectionTestUtils.setField(request, "authorizationCode", "synthetic-code");
        ReflectionTestUtils.setField(request, "platformType", PlatformType.WEB);
        ReflectionTestUtils.setField(request, "nickname", "synthetic-nickname");
        ReflectionTestUtils.setField(request, "agreedTermsTypes", Set.of());
        assertConfigurationError(() -> new CreateUserWithSocial(
            userRepository, socialRepository, authenticationManager, cache, tx, terms).execute(request));
        verifyNoInteractions(socialRepository, tx);
        verify(cache, never()).deleteAll(any());
    }

    @Test
    void linkDoesNotAddAccountOnConfigurationError() {
        User user = mock(User.class);
        assertConfigurationError(() -> new LinkSocialAccount(authenticationManager, socialRepository)
            .execute(LinkSocialAccountCommand.of(user, SocialProvider.KAKAO, null, "synthetic-code", PlatformType.WEB)));
        verifyNoInteractions(user, socialRepository);
    }

    private void assertConfigurationError(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(CustomException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
