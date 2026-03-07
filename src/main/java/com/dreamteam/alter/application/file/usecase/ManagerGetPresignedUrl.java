package com.dreamteam.alter.application.file.usecase;

import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerGetPresignedUrlResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.ManagerGetPresignedUrlUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerGetPresignedUrl")
@RequiredArgsConstructor
public class ManagerGetPresignedUrl implements ManagerGetPresignedUrlUseCase {

    private final FileQueryRepository fileQueryRepository;
    private final FileUrlService fileUrlService;

    @Override
    @Transactional(readOnly = true)
    public ManagerGetPresignedUrlResponseDto execute(ManagerActor actor, String fileId) {
        File file = fileQueryRepository.findById(fileId)
            .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUploadedBy().equals(actor.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return ManagerGetPresignedUrlResponseDto.of(fileUrlService.getPresignedUrl(file));
    }
}
