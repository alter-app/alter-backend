package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ALT-285: 토픽 팬아웃("/sub/chat.{roomId}") -> 유저 큐(convertAndSendToUser, "/user/queue/chat.messages")
 * 전환에 대한 실제 STOMP 왕복 통합 테스트.
 *
 * 단위(mock) 테스트는 "Spring 프레임워크 계약이 이렇게 동작할 것이다"라는 정적 판단에 불과하다 —
 * 이 프로젝트는 그 가정이 실제로는 틀렸던 사례(ALT-284: 세션 레지스트리 미충전, AFTER_COMMIT 유실)를
 * mock 테스트 전부 통과 상태에서 통합 테스트로만 잡은 전례가 있다.
 *
 * 이 테스트는 임베디드 서버(RANDOM_PORT) + 진짜 STOMP 클라이언트(WebSocketStompClient)로 붙어서
 * JwtChannelInterceptor의 실제 CONNECT 인증, WebSocketConfig의 실제 유저 큐 브로커 설정,
 * SendChatMessage UseCase의 실제 DB 커밋 + AFTER_COMMIT 리스너, RedisChatMessageBroadcaster ->
 * 진짜 Redis pub/sub -> ChatMessageRedisSubscriber -> convertAndSendToUser 까지 전부 실제 코드
 * 경로로 태운다. 대체한 프로덕션 로직은 없다 — 다만 실제로 도달 가능한 Redis 인스턴스가 필요하다
 * (JwtChannelInterceptor의 인증 캐시 조회, PresenceHeartbeatChannelInterceptor의 presence TTL
 * 갱신도 전부 Redis를 실제로 거치므로, 테스트 프로필 기본값인 mock host로는 CONNECT 자체가
 * 성립하지 않는다).
 *
 * testcontainers GenericContainer(redis)로 격리된 전용 인스턴스를 띄우는 방식을 먼저 시도했으나,
 * 이 작업을 수행한 로컬 샌드박스의 Docker Desktop(4.88.1)+docker-java(testcontainers 1.20.6)
 * 조합에서 모든 접속 전략(Unix socket / DockerDesktop / Environment)이 전부
 * "BadRequestException Status 400"(빈 stub 응답)으로 실패해 격리 실행이 불가능했다
 * (같은 소켓에 대한 순수 curl 요청은 200으로 정상 응답하므로 docker-java 클라이언트 쪽 비호환으로
 * 보인다 - Docker Engine 자체나 이 테스트 코드의 문제는 아니다). GitHub Actions 같은 표준 리눅스
 * CI 러너에서는 재현되지 않을 가능성이 높다.
 *
 * 그래서 이 테스트는 host/port를 환경변수(ALTER_TEST_REDIS_HOST/PORT, 기본값은 프로젝트 로컬
 * 개발 컨벤션인 alter-redis 컨테이너의 localhost:26379)로 지정된 Redis에 접속하고,
 * 접속 자체가 안 되면(CI 등 Redis가 없는 환경) BeforeAll에서 Assumptions로 SKIPPED 처리한다 -
 * 거짓 초록(연결 실패를 삼키고 통과한 척)보다 정직한 SKIPPED를 택했다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("채팅 유저 큐 실시간 배달 - 실제 STOMP 왕복 통합 테스트")
class ChatStompUserQueueDeliveryIntegrationTest {

    private static final String REDIS_HOST =
        System.getenv().getOrDefault("ALTER_TEST_REDIS_HOST", "localhost");
    private static final int REDIS_PORT =
        Integer.parseInt(System.getenv().getOrDefault("ALTER_TEST_REDIS_PORT", "26379"));

    @BeforeAll
    static void requireRealRedis() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(REDIS_HOST, REDIS_PORT), 1000);
        } catch (Exception e) {
            Assumptions.abort(
                "이 통합 테스트는 실제 Redis(" + REDIS_HOST + ":" + REDIS_PORT + ")가 필요하다 - "
                    + "로컬 alter-redis 컨테이너를 켜거나 ALTER_TEST_REDIS_HOST/PORT 로 지정해라. 원인: " + e);
        }
    }

    @DynamicPropertySource
    static void overrideRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> REDIS_HOST);
        registry.add("spring.data.redis.port", () -> REDIS_PORT);
        // 테스트 프로필 기본값(false)을 이 테스트에서만 켠다 - 실제 Redis pub/sub 팬아웃 경로를 태우기 위함.
        registry.add("chat.redis.subscriber.enabled", () -> "true");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    private WebSocketStompClient stompClient;
    private final List<StompSession> openSessions = new ArrayList<>();

    @AfterEach
    void tearDown() {
        openSessions.forEach(session -> {
            if (session.isConnected()) {
                session.disconnect();
            }
        });
        openSessions.clear();
    }

    @Test
    @DisplayName("활성 멤버는 실제 프레임을 수신하고, 비멤버·나간 멤버는 같은 목적지를 구독해도 수신하지 못한다")
    void 활성멤버만_유저큐로_실제프레임을_수신한다() throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // given: 그룹 채팅방 + 발신자/활성멤버/나간멤버(비멤버는 애초에 미가입)를 실제 트랜잭션 커밋으로 준비
        record Setup(long roomId, User sender, User activeMember, User nonMember, User leftMember) {
        }
        Setup setup = tx.execute(status -> {
            User sender = userRepository.save(newUser());
            User activeMember = userRepository.save(newUser());
            User nonMember = userRepository.save(newUser());
            User leftMember = userRepository.save(newUser());

            ChatRoom room = chatRoomRepository.save(ChatRoom.createGroup(990001L));
            chatRoomMemberRepository.save(ChatRoomMember.create(room.getId(), sender.getId(), TokenScope.APP));
            chatRoomMemberRepository.save(ChatRoomMember.create(room.getId(), activeMember.getId(), TokenScope.APP));

            ChatRoomMember left = ChatRoomMember.create(room.getId(), leftMember.getId(), TokenScope.APP);
            left.leave();
            chatRoomMemberRepository.save(left);
            // nonMember 는 chat_room_members 에 아예 넣지 않는다 (같은 방을 구경도 한 적 없는 유저).

            return new Setup(room.getId(), sender, activeMember, nonMember, leftMember);
        });
        long roomId = setup.roomId();

        String senderToken = issueAccessToken(setup.sender());
        String activeMemberToken = issueAccessToken(setup.activeMember());
        String nonMemberToken = issueAccessToken(setup.nonMember());
        String leftMemberToken = issueAccessToken(setup.leftMember());

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        // ChatMessageResponse.createdAt 은 LocalDateTime - 기본 ObjectMapper에는 JSR-310 모듈이 없어
        // 역직렬화가 실패한다(프로덕션 ObjectMapper 빈은 Boot가 자동 등록해주지만, 테스트에서 새로
        // 만든 MappingJackson2MessageConverter 는 별개 인스턴스라 직접 등록해야 한다).
        MappingJackson2MessageConverter jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.getObjectMapper().registerModule(new JavaTimeModule());
        stompClient.setMessageConverter(jsonConverter);

        StompSession senderSession = connect(senderToken);
        BlockingQueue<ChatMessageResponse> activeMemberQueue = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessageResponse> nonMemberQueue = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessageResponse> leftMemberQueue = new LinkedBlockingQueue<>();
        connectAndSubscribe(activeMemberToken, activeMemberQueue);
        connectAndSubscribe(nonMemberToken, nonMemberQueue);
        connectAndSubscribe(leftMemberToken, leftMemberQueue);

        // 구독이 서버에 실제로 등록될 시간을 준다 (STOMP SUBSCRIBE 는 receipt 없이 fire-and-forget).
        Thread.sleep(500);

        // when: 실제 STOMP SEND로 /pub/app/send.{roomId} 를 호출한다 (SendChatMessage UseCase 전체 경로).
        String content = "integration-" + UUID.randomUUID();
        senderSession.send("/pub/app/send." + roomId,
            new SendChatMessageRequestDto(content, null, null));

        // then: 활성 멤버는 진짜 유저 큐 프레임을 받는다.
        ChatMessageResponse received = activeMemberQueue.poll(15, TimeUnit.SECONDS);
        assertThat(received)
            .as("활성 멤버는 /user/queue/chat.messages 로 실제 프레임을 받아야 한다")
            .isNotNull();
        assertThat(received.getContent()).isEqualTo(content);
        assertThat(received.getChatRoomId()).isEqualTo(roomId);

        // then: 같은 목적지를 구독했어도 멤버가 아니거나 나간 멤버는 받지 못한다 (토픽 시절 유출의 구조적 차단).
        assertThat(nonMemberQueue.poll(3, TimeUnit.SECONDS))
            .as("방 멤버가 아닌 유저는 같은 유저 큐 목적지를 구독해도 수신하면 안 된다")
            .isNull();
        assertThat(leftMemberQueue.poll(3, TimeUnit.SECONDS))
            .as("나간(leftAt) 멤버는 수신하면 안 된다")
            .isNull();
    }

    @Test
    @DisplayName("SUBSCRIBE 시 /queue/** , /user/queue/** 같은 와일드카드로는 남의 방 메시지를 받지 못한다")
    void 와일드카드_구독으로는_남의_메시지를_받지_못한다() throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // given: 공격자는 이 방과 아무 관련도 없다 (멤버였던 적도 없음). 발신자만 이 방의 멤버.
        record Setup(long roomId, User sender, User attacker) {
        }
        Setup setup = tx.execute(status -> {
            User sender = userRepository.save(newUser());
            User attacker = userRepository.save(newUser());

            ChatRoom room = chatRoomRepository.save(ChatRoom.createGroup(990002L));
            chatRoomMemberRepository.save(ChatRoomMember.create(room.getId(), sender.getId(), TokenScope.APP));

            return new Setup(room.getId(), sender, attacker);
        });
        long roomId = setup.roomId();

        String senderToken = issueAccessToken(setup.sender());
        String attackerToken = issueAccessToken(setup.attacker());

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.getObjectMapper().registerModule(new JavaTimeModule());
        stompClient.setMessageConverter(jsonConverter);

        StompSession senderSession = connect(senderToken);
        BlockingQueue<ChatMessageResponse> brokerWildcardQueue = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessageResponse> userWildcardQueue = new LinkedBlockingQueue<>();
        // 공격자: 인증만 통과한 채로 브로커 prefix 전체("/queue/**")와 /user prefix 와일드카드("/user/queue/**")를
        // 각각 구독한다 - 정상 클라이언트라면 절대 보내지 않을 destination 이다.
        connectAndSubscribe(attackerToken, "/queue/**", brokerWildcardQueue);
        connectAndSubscribe(attackerToken, "/user/queue/**", userWildcardQueue);

        Thread.sleep(500);

        String content = "wildcard-leak-" + UUID.randomUUID();
        senderSession.send("/pub/app/send." + roomId,
            new SendChatMessageRequestDto(content, null, null));

        assertThat(brokerWildcardQueue.poll(5, TimeUnit.SECONDS))
            .as("/queue/** 구독은 공격자와 무관한 방의 메시지를 받으면 안 된다")
            .isNull();
        assertThat(userWildcardQueue.poll(3, TimeUnit.SECONDS))
            .as("/user/queue/** 구독은 공격자와 무관한 방의 메시지를 받으면 안 된다")
            .isNull();
    }

    private String issueAccessToken(User user) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> authService.generateAuthorization(user, TokenScope.APP).getAccessToken());
    }

    private StompSession connect(String accessToken) throws Exception {
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + accessToken);

        StompSession session = stompClient
            .connectAsync("ws://localhost:" + port + "/api/ws-connect", handshakeHeaders, connectHeaders,
                new StompSessionHandlerAdapter() {
                })
            .get(10, TimeUnit.SECONDS);
        openSessions.add(session);
        return session;
    }

    private StompSession connectAndSubscribe(String accessToken, BlockingQueue<ChatMessageResponse> queue)
        throws Exception {
        return connectAndSubscribe(accessToken, "/user/queue/chat.messages", queue);
    }

    private StompSession connectAndSubscribe(
        String accessToken, String destination, BlockingQueue<ChatMessageResponse> queue
    ) throws Exception {
        StompSession session = connect(accessToken);
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.offer((ChatMessageResponse) payload);
            }
        });
        return session;
    }

    private static final AtomicLong USER_SEQ = new AtomicLong();

    private User newUser() {
        long seq = System.nanoTime() + USER_SEQ.incrementAndGet();
        return User.create(
            "010" + String.format("%08d", seq % 100000000L),
            "encoded-password",
            "테스트유저" + seq,
            "nick" + seq,
            UserGender.GENDER_MALE,
            "19900101",
            "user" + seq + "@example.com"
        );
    }
}
