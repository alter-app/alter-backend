package com.dreamteam.alter.common.config;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ChatWebSocketSessionRegistry 테스트")
class ChatWebSocketSessionRegistryTests {

    private final ChatWebSocketSessionRegistry sut = new ChatWebSocketSessionRegistry();

    private WebSocketSession session(String sessionId) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        return session;
    }

    @Test
    @DisplayName("registerSession + linkMember를 모두 거쳐야 findLocalSessions로 조회된다")
    void registerSession_linkMember_조회됨() {
        // given
        WebSocketSession session = session("sess1");

        // when: raw WS 연결(sessionId만 앎) + STOMP CONNECT 인증(유저를 앎)이 각각 채운다
        sut.registerSession(session);
        sut.linkMember(TokenScope.APP, 1L, "sess1");

        // then
        assertThat(sut.findLocalSessions(TokenScope.APP, 1L)).containsExactly(session);
    }

    @Test
    @DisplayName("linkMember만 있고 registerSession이 없으면(=이 인스턴스가 물고 있지 않으면) 조회되지 않는다")
    void linkMember만으로는_조회안됨() {
        // when: 다른 인스턴스가 물고 있는 세션의 sessionId만 알게 된 상황을 흉내
        sut.linkMember(TokenScope.APP, 1L, "sess-on-other-instance");

        // then
        assertThat(sut.findLocalSessions(TokenScope.APP, 1L)).isEmpty();
    }

    @Test
    @DisplayName("같은 유저의 세션 2개를 등록하면 둘 다 조회된다")
    void register_같은유저_세션2개() {
        // given
        WebSocketSession session1 = session("sess1");
        WebSocketSession session2 = session("sess2");

        // when
        sut.registerSession(session1);
        sut.linkMember(TokenScope.APP, 1L, "sess1");
        sut.registerSession(session2);
        sut.linkMember(TokenScope.APP, 1L, "sess2");

        // then
        assertThat(sut.findLocalSessions(TokenScope.APP, 1L)).containsExactlyInAnyOrder(session1, session2);
    }

    @Test
    @DisplayName("unregisterSession 하면 더 이상 조회되지 않는다")
    void unregisterSession_조회안됨() {
        // given
        WebSocketSession session = session("sess1");
        sut.registerSession(session);
        sut.linkMember(TokenScope.APP, 1L, "sess1");

        // when
        sut.unregisterSession(session);

        // then
        assertThat(sut.findLocalSessions(TokenScope.APP, 1L)).isEmpty();
    }

    @Test
    @DisplayName("unlinkMember 하면 더 이상 조회되지 않는다")
    void unlinkMember_조회안됨() {
        // given
        WebSocketSession session = session("sess1");
        sut.registerSession(session);
        sut.linkMember(TokenScope.APP, 1L, "sess1");

        // when
        sut.unlinkMember(TokenScope.APP, 1L, "sess1");

        // then
        assertThat(sut.findLocalSessions(TokenScope.APP, 1L)).isEmpty();
    }

    @Test
    @DisplayName("같은 유저의 세션 중 하나만 unlinkMember하면 나머지는 조회된다")
    void unlinkMember_일부만_제거() {
        // given
        WebSocketSession session1 = session("sess1");
        WebSocketSession session2 = session("sess2");
        sut.registerSession(session1);
        sut.linkMember(TokenScope.APP, 1L, "sess1");
        sut.registerSession(session2);
        sut.linkMember(TokenScope.APP, 1L, "sess2");

        // when
        sut.unlinkMember(TokenScope.APP, 1L, "sess1");

        // then
        assertThat(sut.findLocalSessions(TokenScope.APP, 1L)).containsExactly(session2);
    }

    @Test
    @DisplayName("다른 유저의 세션은 조회되지 않는다")
    void findLocalSessions_다른유저는_미조회() {
        // given
        WebSocketSession session = session("sess1");
        sut.registerSession(session);
        sut.linkMember(TokenScope.APP, 1L, "sess1");

        // when & then
        assertThat(sut.findLocalSessions(TokenScope.APP, 2L)).isEmpty();
        assertThat(sut.findLocalSessions(TokenScope.MANAGER, 1L)).isEmpty();
    }

    @Test
    @DisplayName("등록된 적 없는 유저의 findLocalSessions는 빈 리스트를 반환한다")
    void findLocalSessions_없으면_빈리스트() {
        assertThat(sut.findLocalSessions(TokenScope.APP, 999L)).isEqualTo(List.of());
    }
}
