package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "채팅방 정보 응답 DTO")
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "12")
    private Long id;

    @Schema(description = "채팅방 타입 (DIRECT: 개인 채팅, GROUP: 업장 단톡방)")
    private DescribedEnumDto<ChatRoomType> type;

    @Schema(description = "채팅방 표시명 (개인 채팅은 상대방 이름, 그룹 채팅은 업장명)", example = "알터 카페 강남점")
    private String roomName;

    @Schema(description = "채팅방 참여 인원 수 (나간 멤버 제외)", example = "8")
    private int memberCount;

    @Schema(description = "상대방 ID (그룹 채팅은 null)", example = "101")
    private Long opponentId;

    @Schema(description = "상대방 스코프 (그룹 채팅은 null)", example = "APP")
    private DescribedEnumDto<TokenScope> opponentScope;

    @Schema(description = "상대방 이름 (그룹 채팅은 null)", example = "김알바")
    private String opponentName;

    @Schema(description = "상대방 프로필 이미지 URL (그룹 채팅이거나 미등록 시 null)")
    private String opponentProfileImageUrl;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    @Schema(description = "수정일")
    private LocalDateTime updatedAt;

    public static ChatRoomResponseDto of(
        ChatRoom chatRoom,
        int memberCount,
        String roomName,
        Long opponentId,
        TokenScope opponentScope,
        String opponentName,
        String opponentProfileImageUrl
    ) {
        return ChatRoomResponseDto.builder()
            .id(chatRoom.getId())
            .type(DescribedEnumDto.of(chatRoom.getType(), ChatRoomType.describe()))
            .roomName(roomName)
            .memberCount(memberCount)
            .opponentId(opponentId)
            .opponentScope(opponentScope != null ? DescribedEnumDto.of(opponentScope, TokenScope.describe()) : null)
            .opponentName(opponentName)
            .opponentProfileImageUrl(opponentProfileImageUrl)
            .createdAt(chatRoom.getCreatedAt())
            .updatedAt(chatRoom.getUpdatedAt())
            .build();
    }
}
