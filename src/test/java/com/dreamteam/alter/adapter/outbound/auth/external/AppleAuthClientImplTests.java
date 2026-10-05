package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.type.PlatformType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
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
class AppleAuthClientImplTests {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private AppleAuthClientImpl client;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256);
        String pem = "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(generator.generateKeyPair().getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----";
        client = new AppleAuthClientImpl(restTemplate,
            Base64.getEncoder().encodeToString(pem.getBytes(StandardCharsets.UTF_8)), new ObjectMapper());
        ReflectionTestUtils.setField(client, "appleServiceId", "synthetic-service");
        ReflectionTestUtils.setField(client, "appleClientId", "synthetic-bundle");
        ReflectionTestUtils.setField(client, "appleTeamId", "synthetic-team");
        ReflectionTestUtils.setField(client, "appleLoginKey", "synthetic-kid");
        ReflectionTestUtils.setField(client, "appleRedirectUri", "https://example.invalid/callback");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid_client", "invalid_request", "invalid_grant", "unauthorized_client", "unsupported_grant_type", "invalid_scope"})
    void providerErrorsDoNotAssertUnprovenCodeExpiry(String code, CapturedOutput output) {
        failWith("{\"error\":\"" + code + "\",\"error_description\":\"synthetic-private-code synthetic-private-key\"}");
        assertError();
        assertThat(output.getAll()).contains("provider=APPLE", "stage=token", "httpStatus=400", "providerErrorCode=" + code)
            .doesNotContain("synthetic-private-code", "synthetic-private-key", "synthetic-authorization-code", "synthetic-private-message");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-json", "{}", "{\"error\":\"synthetic-private-key\"}", "{\"error\":\"invalid_client\\nsynthetic-private-key\"}"})
    void unparseableOrUnsafeProviderCodeIsUnknown(String body, CapturedOutput output) {
        failWith(body);
        assertError();
        assertThat(output.getAll()).contains("providerErrorCode=unknown").doesNotContain("synthetic-private-key");
    }

    @ParameterizedTest
    @EnumSource(PlatformType.class)
    void successfulTokenExchangePreservesResponse(PlatformType platform) {
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("{\"refresh_token\":\"synthetic-refresh\",\"id_token\":\"synthetic-identity\"}"));
        var result = client.exchangeCodeForToken("synthetic-authorization-code", platform);
        assertThat(result.getRefreshToken()).isEqualTo("synthetic-refresh");
        assertThat(result.getIdentityToken()).isEqualTo("synthetic-identity");
    }

    private void failWith(String body) {
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
            .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "synthetic-private-message",
                HttpHeaders.EMPTY, body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
    }

    private void assertError() {
        assertThatThrownBy(() -> client.exchangeCodeForToken("synthetic-authorization-code", PlatformType.WEB))
            .isInstanceOfSatisfying(CustomException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
