package com.dreamteam.alter.domain.file.port.inbound;

import com.dreamteam.alter.domain.file.type.FileTargetType;

import java.util.List;

public interface AttachFilesUseCase {
    void execute(List<String> fileIds, FileTargetType targetType, String targetId, Long userId);
}
