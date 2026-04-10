package com.dreamteam.alter.application.file.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.common.dto.FilePresignedUrlResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AppGetPresignedUrlUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;

import lombok.RequiredArgsConstructor;

@Service("appGetPresignedUrl")
@RequiredArgsConstructor
public class AppGetPresignedUrl implements AppGetPresignedUrlUseCase {

    private final FileQueryRepository fileQueryRepository;
    private final FileUrlService fileUrlService;

    @Override
    @Transactional(readOnly = true)
    public FilePresignedUrlResponseDto execute(AppActor actor, String fileId) {
        File file = fileQueryRepository.findById(fileId)
            .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUploadedBy().equals(actor.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return FilePresignedUrlResponseDto.of(fileUrlService.getPresignedUrl(file));
    }
}
