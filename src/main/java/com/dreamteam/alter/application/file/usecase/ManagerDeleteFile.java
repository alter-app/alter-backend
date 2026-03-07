package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.ManagerDeleteFileUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerDeleteFile")
@RequiredArgsConstructor
public class ManagerDeleteFile implements ManagerDeleteFileUseCase {

    private final FileQueryRepository fileQueryRepository;
    private final FileDeleteService fileDeleteService;

    @Override
    @Transactional
    public void execute(ManagerActor actor, String fileId) {
        File file = fileQueryRepository.findById(fileId)
            .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUploadedBy().equals(actor.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        fileDeleteService.delete(file);
    }
}
