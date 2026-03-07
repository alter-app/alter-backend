package com.dreamteam.alter.adapter.inbound.admin.file.controller;

import com.dreamteam.alter.adapter.inbound.admin.file.dto.AdminGetPresignedUrlResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.file.dto.AdminUploadFileResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.application.aop.AdminActionContext;
import com.dreamteam.alter.domain.file.port.inbound.AdminDeleteFileUseCase;
import com.dreamteam.alter.domain.file.port.inbound.AdminGetPresignedUrlUseCase;
import com.dreamteam.alter.domain.file.port.inbound.AdminUploadFileUseCase;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AdminActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/admin/files")
@PreAuthorize("hasAnyRole('ADMIN')")
@RequiredArgsConstructor
public class AdminFileController implements AdminFileControllerSpec {

    @Resource(name = "adminUploadFile")
    private final AdminUploadFileUseCase adminUploadFile;

    @Resource(name = "adminGetPresignedUrl")
    private final AdminGetPresignedUrlUseCase adminGetPresignedUrl;

    @Resource(name = "adminDeleteFile")
    private final AdminDeleteFileUseCase adminDeleteFile;

    @Override
    @PostMapping
    public ResponseEntity<CommonApiResponse<AdminUploadFileResponseDto>> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("targetType") FileTargetType targetType,
        @RequestParam("bucketType") BucketType bucketType
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(adminUploadFile.execute(actor, file, targetType, bucketType)));
    }

    @Override
    @GetMapping("/{fileId}/presigned-url")
    public ResponseEntity<CommonApiResponse<AdminGetPresignedUrlResponseDto>> getPresignedUrl(
        @PathVariable String fileId
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(adminGetPresignedUrl.execute(actor, fileId)));
    }

    @Override
    @DeleteMapping("/{fileId}")
    public ResponseEntity<CommonApiResponse<Void>> deleteFile(
        @PathVariable String fileId
    ) {
        AdminActor actor = AdminActionContext.getInstance().getActor();
        adminDeleteFile.execute(actor, fileId);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
