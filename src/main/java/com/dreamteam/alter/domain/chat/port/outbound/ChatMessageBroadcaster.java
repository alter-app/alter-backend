package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;

import java.util.List;

public interface ChatMessageBroadcaster {

    /**
     * @param recipientNames 수신자 principal 이름({@link com.dreamteam.alter.domain.auth.type.TokenScope#principalName})
     *                       목록. 발신자 본인도 포함한다. 발송 인스턴스에서 1회만 계산한 값을 넘긴다.
     */
    void broadcast(Long roomId, ChatMessageResponse message, List<String> recipientNames);
}
