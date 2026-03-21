package com.dreamteam.alter.adapter.inbound.general.file.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.FilePresignedUrlResponseDto;
import com.dreamteam.alter.adapter.inbound.general.file.dto.AppUploadFileResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.file.port.inbound.AppDeleteFileUseCase;
import com.dreamteam.alter.domain.file.port.inbound.AppGetPresignedUrlUseCase;
import com.dreamteam.alter.domain.file.port.inbound.AppUploadFileUseCase;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/app/files")
@RequiredArgsConstructor
public class AppFileController implements AppFileControllerSpec {

    @Resource(name = "appUploadFile")
    private final AppUploadFileUseCase appUploadFile;

    @Resource(name = "appGetPresignedUrl")
    private final AppGetPresignedUrlUseCase appGetPresignedUrl;

    @Resource(name = "appDeleteFile")
    private final AppDeleteFileUseCase appDeleteFile;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<AppUploadFileResponseDto>> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("targetType") FileTargetType targetType,
        @RequestParam("bucketType") BucketType bucketType
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(appUploadFile.execute(actor, file, targetType, bucketType)));
    }

    @Override
    @GetMapping("/{fileId}/presigned-url")
    public ResponseEntity<CommonApiResponse<FilePresignedUrlResponseDto>> getPresignedUrl(
        @PathVariable String fileId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(appGetPresignedUrl.execute(actor, fileId)));
    }

    @Override
    @DeleteMapping("/{fileId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteFile(
        @PathVariable String fileId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        appDeleteFile.execute(actor, fileId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
