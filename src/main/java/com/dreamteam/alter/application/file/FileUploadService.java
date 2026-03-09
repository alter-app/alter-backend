package com.dreamteam.alter.application.file;

import com.dreamteam.alter.common.util.FileValidator;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("fileUploadService")
@RequiredArgsConstructor
public class FileUploadService {

    private final FileRepository fileRepository;

    @Resource(name = "s3Client")
    private final S3Client s3Client;

    @Transactional
    public String upload(MultipartFile file, FileTargetType targetType, BucketType bucketType, Long userId) {
        FileValidator.validate(file);

        String originalFileName = FileValidator.sanitizeFileName(file.getOriginalFilename());
        String extension = FileValidator.extractExtension(originalFileName);
        String storedKey = FileValidator.buildStoredKey(targetType, extension);

        String fileUrl = s3Client.upload(file, storedKey, bucketType);

        File savedFile = fileRepository.save(File.create(
            targetType,
            originalFileName,
            storedKey,
            fileUrl,
            file.getContentType(),
            file.getSize(),
            bucketType,
            userId
        ));

        return savedFile.getId();
    }
}
