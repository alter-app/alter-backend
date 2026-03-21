package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("attachFiles")
@RequiredArgsConstructor
public class AttachFiles implements AttachFilesUseCase {

    private final FileQueryRepository fileQueryRepository;

    @Override
    @Transactional
    public void execute(List<String> fileIds, FileTargetType targetType, String targetId, Long userId) {
        List<File> files = fileQueryRepository.findAllByIdIn(fileIds);

        if (files.size() != fileIds.size()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        for (File file : files) {
            validateFile(file, userId, targetType);
            file.attach(targetId);
        }
    }

    @Override
    @Transactional
    public void executeMap(Map<String, FileTargetType> fileIdsMap, String targetId, Long userId) {
        List<File> files = fileQueryRepository.findAllByIdIn(fileIdsMap.keySet().stream().toList());

        if (files.size() != fileIdsMap.size()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        for (File file : files) {
            validateFile(file, userId, fileIdsMap.get(file.getId()));
            file.attach(targetId);
        }
    }

    private void validateFile(File file, Long userId, FileTargetType targetType) {
        if (!file.getUploadedBy().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (file.getStatus() != FileStatus.PENDING) {
            throw new CustomException(ErrorCode.FILE_ALREADY_ATTACHED);
        }
        if (file.getTargetType() != targetType) {
            throw new CustomException(ErrorCode.INVALID_FILE);
        }
    }
}
