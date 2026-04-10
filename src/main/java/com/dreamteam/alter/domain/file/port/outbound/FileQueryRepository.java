package com.dreamteam.alter.domain.file.port.outbound;

import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.type.FileTargetType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileQueryRepository {
    Optional<File> findById(String id);
    List<File> findAllByIdIn(List<String> ids);
    List<File> findAllByTargetTypeAndTargetId(FileTargetType targetType, String targetId);
    List<File> findOrphanFiles(LocalDateTime before);

	Optional<File> findByTargetTypeAndTargetId(FileTargetType fileTargetType, String targetId);
}
