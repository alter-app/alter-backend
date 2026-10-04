package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class KakaoAuthClientImplTests {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final KakaoAuthClientImpl client = new KakaoAuthClientImpl(new ObjectMapper(), restTemplate);

    @ParameterizedTest
    @ValueSource(strings = {"KOE303", "KOE310", "KOE101", "KOE010", "KOE237", "KOE999"})
    void configurationAndUnknownErrorsAreNotCodeExpiry(String code, CapturedOutput output) {
        failWith("{\"error\":\"invalid_grant\",\"error_code\":\"" + code
            + "\",\"error_description\":\"synthetic-private-code synthetic-private-key\"}");
        assertError(ErrorCode.INTERNAL_SERVER_ERROR);
        assertThat(output.getAll()).contains("provider=KAKAO", "stage=token", "httpStatus=400", "providerErrorCode=" + code)
            .doesNotContain("synthetic-private-code", "synthetic-private-key", "synthetic-authorization-code");
    }

    @Test
    void onlyExplicitInvalidCodeRemainsExpiry(CapturedOutput output) {
        failWith("{\"error\":\"invalid_grant\",\"error_code\":\"KOE320\",\"error_description\":\"synthetic-private-code\"}");
        assertError(ErrorCode.SOCIAL_AUTH_CODE_EXPIRED);
        assertThat(output.getAll()).contains("providerErrorCode=KOE320").doesNotContain("synthetic-private-code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-json", "{}", "{\"error\":\"invalid_grant\"}", "{\"error_code\":\"synthetic-private-key\\nKOE320\"}"})
    void unparseableOrUnsafeProviderCodeIsUnknown(String body, CapturedOutput output) {
        failWith(body);
        assertError(ErrorCode.INTERNAL_SERVER_ERROR);
        assertThat(output.getAll()).contains("providerErrorCode=unknown").doesNotContain("synthetic-private-key");
    }

    @ParameterizedTest
    @EnumSource(PlatformType.class)
    void successfulTokenExchangePreservesResponse(PlatformType platform) {
        ReflectionTestUtils.setField(client, "kakaoClientId", "synthetic-client");
        ReflectionTestUtils.setField(client, "kakaoRedirectUri", "https://example.invalid/callback");
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("{\"access_token\":\"synthetic-access\",\"refresh_token\":\"synthetic-refresh\"}"));
        var result = client.exchangeCodeForToken("synthetic-authorization-code", platform);
        assertThat(result.getAccessToken()).isEqualTo("synthetic-access");
        assertThat(result.getRefreshToken()).isEqualTo("synthetic-refresh");
    }

    private void failWith(String body) {
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
            .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "synthetic-private-message",
                HttpHeaders.EMPTY, body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
    }

    private void assertError(ErrorCode expected) {
        assertThatThrownBy(() -> client.exchangeCodeForToken("synthetic-authorization-code", PlatformType.WEB))
            .isInstanceOfSatisfying(CustomException.class, e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }
}
