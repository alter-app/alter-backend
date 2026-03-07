package com.dreamteam.alter.adapter.inbound.manager.file.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerGetPresignedUrlResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerUploadFileResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.file.port.inbound.ManagerDeleteFileUseCase;
import com.dreamteam.alter.domain.file.port.inbound.ManagerGetPresignedUrlUseCase;
import com.dreamteam.alter.domain.file.port.inbound.ManagerUploadFileUseCase;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/manager/files")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
public class ManagerFileController implements ManagerFileControllerSpec {

    @Resource(name = "managerUploadFile")
    private final ManagerUploadFileUseCase managerUploadFile;

    @Resource(name = "managerGetPresignedUrl")
    private final ManagerGetPresignedUrlUseCase managerGetPresignedUrl;

    @Resource(name = "managerDeleteFile")
    private final ManagerDeleteFileUseCase managerDeleteFile;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<ManagerUploadFileResponseDto>> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("targetType") FileTargetType targetType,
        @RequestParam("bucketType") BucketType bucketType
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(managerUploadFile.execute(actor, file, targetType, bucketType)));
    }

    @Override
    @GetMapping("/{fileId}/presigned-url")
    public ResponseEntity<CommonApiResponse<ManagerGetPresignedUrlResponseDto>> getPresignedUrl(
        @PathVariable String fileId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(managerGetPresignedUrl.execute(actor, fileId)));
    }

    @Override
    @DeleteMapping("/{fileId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteFile(
        @PathVariable String fileId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        managerDeleteFile.execute(actor, fileId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
