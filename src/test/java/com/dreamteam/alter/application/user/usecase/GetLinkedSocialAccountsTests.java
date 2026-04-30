package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.dto.SocialAccountStatusDto;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLinkedSocialAccounts 테스트")
class GetLinkedSocialAccountsTests {

    @Mock
    private UserSocialQueryRepository userSocialQueryRepository;

    @InjectMocks
    private GetLinkedSocialAccounts getLinkedSocialAccounts;

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        given(user.getId()).willReturn(1L);
    }

    private User command() {
        return user;
    }

    @Test
    @DisplayName("연동된 플랫폼은 linkedAt과 함께, 미연동 플랫폼은 linkedAt이 null로 반환된다")
    void execute_returnsStatusForAllProviders() {
        // given
        LocalDateTime kakaoLinkedAt = LocalDateTime.of(2026, 4, 1, 12, 34, 56);
        given(userSocialQueryRepository.findLinkedSocialAccountsByUserId(1L))
            .willReturn(Map.of(SocialProvider.KAKAO, kakaoLinkedAt));

        // when
        List<SocialAccountStatusDto> result = getLinkedSocialAccounts.execute(command());

        // then
        assertThat(result).hasSize(SocialProvider.values().length);

        Map<SocialProvider, SocialAccountStatusDto> dtoMap = result.stream()
            .collect(Collectors.toMap(SocialAccountStatusDto::getProvider, dto -> dto));

        assertThat(dtoMap.get(SocialProvider.KAKAO).isLinked()).isTrue();
        assertThat(dtoMap.get(SocialProvider.KAKAO).getLinkedAt()).isEqualTo(kakaoLinkedAt);
        assertThat(dtoMap.get(SocialProvider.APPLE).isLinked()).isFalse();
        assertThat(dtoMap.get(SocialProvider.APPLE).getLinkedAt()).isNull();
    }

    @Test
    @DisplayName("연동된 소셜 계정이 없으면 모두 linked=false, linkedAt=null 로 반환한다")
    void execute_withNoLinkedAccounts_returnsAllFalse() {
        // given
        given(userSocialQueryRepository.findLinkedSocialAccountsByUserId(1L))
            .willReturn(Map.of());

        // when
        List<SocialAccountStatusDto> result = getLinkedSocialAccounts.execute(command());

        // then
        assertThat(result).hasSize(SocialProvider.values().length);
        assertThat(result).allMatch(dto -> !dto.isLinked() && dto.getLinkedAt() == null);
    }

    @Test
    @DisplayName("모든 소셜 계정이 연동되어 있으면 모두 linked=true이며 linkedAt이 채워진다")
    void execute_withAllLinkedAccounts_returnsAllTrue() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 4, 30, 0, 0, 0);
        Map<SocialProvider, LocalDateTime> linked = new HashMap<>();
        for (SocialProvider provider : SocialProvider.values()) {
            linked.put(provider, now);
        }
        given(userSocialQueryRepository.findLinkedSocialAccountsByUserId(1L)).willReturn(linked);

        // when
        List<SocialAccountStatusDto> result = getLinkedSocialAccounts.execute(command());

        // then
        assertThat(result).hasSize(SocialProvider.values().length);
        assertThat(result).allMatch(dto -> dto.isLinked() && now.equals(dto.getLinkedAt()));
        assertThat(result.stream().map(SocialAccountStatusDto::getProvider).toList())
            .containsExactlyInAnyOrder(Arrays.stream(SocialProvider.values()).toArray(SocialProvider[]::new));
    }
}
