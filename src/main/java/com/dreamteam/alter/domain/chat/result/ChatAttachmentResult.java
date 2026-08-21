package com.dreamteam.alter.domain.chat.result;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;

public record ChatAttachmentResult(String fileId, String url) {
    public static ChatAttachmentResult from(FileResponseDto file) {
        return new ChatAttachmentResult(file.getFileId(), file.getUrl());
    }
}
