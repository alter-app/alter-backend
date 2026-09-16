package com.dreamteam.alter.application.auth.token;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("AccessTokenAuthentication 테스트")
class AccessTokenAuthenticationTests {

    @Test
    @DisplayName("인증된 토큰의 getName()은 scope:id 형식이며 토큰 문자열을 포함하지 않는다")
    void getName_인증됨_scope와id조합() {
        // given
        LoginUserDto loginUser = mock(LoginUserDto.class);
        given(loginUser.getScope()).willReturn(TokenScope.APP);
        given(loginUser.getId()).willReturn(123L);
        AccessTokenAuthentication authentication =
            new AccessTokenAuthentication("access-token-value", loginUser, Collections.emptyList());

        // when
        String name = authentication.getName();

        // then
        assertThat(name).isEqualTo("APP:123");
        assertThat(name).doesNotContain("access-token-value");
    }

    @Test
    @DisplayName("같은 유저의 서로 다른 토큰이어도 getName()은 동일하다")
    void getName_토큰이달라도_같은유저면_동일() {
        // given
        LoginUserDto loginUser = mock(LoginUserDto.class);
        given(loginUser.getScope()).willReturn(TokenScope.MANAGER);
        given(loginUser.getId()).willReturn(456L);
        AccessTokenAuthentication first =
            new AccessTokenAuthentication("token-a", loginUser, Collections.emptyList());
        AccessTokenAuthentication second =
            new AccessTokenAuthentication("token-b", loginUser, Collections.emptyList());

        // when & then
        assertThat(first.getName()).isEqualTo(second.getName());
    }

    @Test
    @DisplayName("details가 없는 미인증 토큰은 getName()이 빈 문자열이고 토큰 문자열을 노출하지 않는다")
    void getName_details없음_빈문자열() {
        // given
        AccessTokenAuthentication authentication = new AccessTokenAuthentication("raw-access-token");

        // when
        String name = authentication.getName();

        // then
        assertThat(name).isEmpty();
    }

    @Test
    @DisplayName("getName()과 채팅 수신자 이름(ChatRoomMember 기반)은 같은 유저에 대해 동일한 문자열을 만든다 " +
        "(발행측/수신측 이름 조립이 갈라지면 convertAndSendToUser가 조용히 실패한다)")
    void getName_채팅수신자이름과_동일() {
        // given
        LoginUserDto loginUser = mock(LoginUserDto.class);
        given(loginUser.getScope()).willReturn(TokenScope.APP);
        given(loginUser.getId()).willReturn(789L);
        AccessTokenAuthentication authentication =
            new AccessTokenAuthentication("access-token-value", loginUser, Collections.emptyList());

        ChatRoomMember member = ChatRoomMember.create(1L, 789L, TokenScope.APP);

        // when
        String subscriberName = authentication.getName();
        String recipientName = member.getMemberScope().principalName(member.getMemberId());

        // then
        assertThat(subscriberName).isEqualTo(recipientName);
    }

}
