package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomListResponseDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMyChatRoomList 테스트")
class GetMyChatRoomListTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatMessageQueryRepository chatMessageQueryRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

    private GetMyChatRoomList sut;

    @BeforeEach
    void setUp() {
        sut = new GetMyChatRoomList(
            chatRoomQueryRepository,
            chatMessageQueryRepository,
            chatRoomMemberQueryRepository,
            fileQueryRepository,
            fileUrlService,
            new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    @Test
    @DisplayName("DIRECT 채팅방은 상대방 이름이 roomName이 되고 프로필 URL이 매핑된다")
    void execute_DIRECT_상대방정보와_프로필URL_매핑() {
        // given
        Long participantId = 10L;
        AppActor actor = new AppActor(participantId, null, null);

        Long chatRoomId = 1L;
        Long opponentId = 201L;
        ChatRoomListWithOpponentResponse directRoom = new ChatRoomListWithOpponentResponse(
            chatRoomId, ChatRoomType.DIRECT, LocalDateTime.now(), LocalDateTime.now(),
            null, opponentId, TokenScope.APP, "김알바", null, null, 0
        );

        given(chatRoomQueryRepository.countChatRoomsByParticipant(participantId, TokenScope.APP)).willReturn(1L);
        given(chatRoomQueryRepository.getChatRoomListWithOpponent(any(), any(), any()))
            .willReturn(List.of(directRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoomIds(List.of(chatRoomId)))
            .willReturn(Map.of(chatRoomId, 3L));

        File profileFile = File.create(
            FileTargetType.USER_PROFILE, "profile.png", "stored/profile.png",
            "https://cdn.example.com/profile.png", "image/png", 1024L, BucketType.PUBLIC, opponentId
        );
        profileFile.attach(String.valueOf(opponentId));
        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, List.of(String.valueOf(opponentId))))
            .willReturn(List.of(profileFile));
        given(fileUrlService.resolve(profileFile))
            .willReturn(FileResponseDto.of(profileFile, "https://cdn.example.com/profile.png"));

        // when
        CursorPaginatedApiResponse<ChatRoomListResponseDto> response =
            sut.execute(actor, CursorPageRequestDto.of(null, 10));

        // then
        ChatRoomListResponseDto dto = response.data().getFirst();
        assertThat(dto.getType().value()).isEqualTo(ChatRoomType.DIRECT);
        assertThat(dto.getRoomName()).isEqualTo("김알바");
        assertThat(dto.getMemberCount()).isEqualTo(3);
        assertThat(dto.getOpponentProfileImageUrl()).isEqualTo("https://cdn.example.com/profile.png");
    }

    @Test
    @DisplayName("GROUP 채팅방은 업장명이 roomName이 되고 상대방 필드는 모두 null이다")
    void execute_GROUP_업장명과_null상대방필드() {
        // given
        Long participantId = 10L;
        AppActor actor = new AppActor(participantId, null, null);

        Long chatRoomId = 2L;
        ChatRoomListWithOpponentResponse groupRoom = new ChatRoomListWithOpponentResponse(
            chatRoomId, ChatRoomType.GROUP, LocalDateTime.now(), LocalDateTime.now(),
            "알터 카페 강남점", null, null, null, null, null, 0
        );

        given(chatRoomQueryRepository.countChatRoomsByParticipant(participantId, TokenScope.APP)).willReturn(1L);
        given(chatRoomQueryRepository.getChatRoomListWithOpponent(any(), any(), any()))
            .willReturn(List.of(groupRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoomIds(List.of(chatRoomId)))
            .willReturn(Map.of(chatRoomId, 8L));

        // when
        CursorPaginatedApiResponse<ChatRoomListResponseDto> response =
            sut.execute(actor, CursorPageRequestDto.of(null, 10));

        // then
        ChatRoomListResponseDto dto = response.data().getFirst();
        assertThat(dto.getRoomName()).isEqualTo("알터 카페 강남점");
        assertThat(dto.getOpponentId()).isNull();
        assertThat(dto.getOpponentName()).isNull();
        assertThat(dto.getOpponentProfileImageUrl()).isNull();
        assertThat(dto.getMemberCount()).isEqualTo(8);

        // GROUP 방은 프로필 조회 대상에서 제외된다 (N+1 방지 대상 아님)
        verify(fileQueryRepository).findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, List.of());
    }

    @Test
    @DisplayName("totalCount는 count 쿼리 결과로 채워진다 (현재 페이지 건수와 다르다)")
    void execute_totalCount_count쿼리결과로_채워짐() {
        // given
        Long participantId = 10L;
        AppActor actor = new AppActor(participantId, null, null);

        Long chatRoomId = 3L;
        ChatRoomListWithOpponentResponse room = new ChatRoomListWithOpponentResponse(
            chatRoomId, ChatRoomType.GROUP, LocalDateTime.now(), LocalDateTime.now(),
            "알터 카페 홍대점", null, null, null, null, null, 0
        );

        // count 쿼리 결과(50)와 실제 조회된 페이지 건수(1)를 의도적으로 다르게 세팅
        given(chatRoomQueryRepository.countChatRoomsByParticipant(participantId, TokenScope.APP)).willReturn(50L);
        given(chatRoomQueryRepository.getChatRoomListWithOpponent(any(), any(), any()))
            .willReturn(List.of(room));

        // when
        CursorPaginatedApiResponse<ChatRoomListResponseDto> response =
            sut.execute(actor, CursorPageRequestDto.of(null, 10));

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.page().totalCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("count가 0이면 목록 조회를 호출하지 않고 빈 응답을 반환한다")
    void execute_count가0이면_목록조회_생략하고_빈응답() {
        // given
        Long participantId = 10L;
        AppActor actor = new AppActor(participantId, null, null);

        given(chatRoomQueryRepository.countChatRoomsByParticipant(participantId, TokenScope.APP)).willReturn(0L);

        // when
        CursorPaginatedApiResponse<ChatRoomListResponseDto> response =
            sut.execute(actor, CursorPageRequestDto.of(null, 10));

        // then
        assertThat(response.data()).isEmpty();
        assertThat(response.page().totalCount()).isEqualTo(0);
        verify(chatRoomQueryRepository, never()).getChatRoomListWithOpponent(any(), any(), any());
    }
}
