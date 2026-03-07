package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

public class FileValidator {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "application/pdf"
    );

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        return Paths.get(fileName).getFileName().toString();
    }

    public static String buildStoredKey(FileTargetType targetType, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return targetType.name().toLowerCase() + "/" + uuid + extension;
    }

    public static String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return "." + fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
