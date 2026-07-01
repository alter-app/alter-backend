package com.dreamteam.alter.domain.file.entity;

import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class File {

    @Id
    @Column(name = "id", length = 36, nullable = false, unique = true)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 40, nullable = false)
    private FileTargetType targetType;

    @Column(name = "target_id", length = 36)
    private String targetId;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_key", length = 512, nullable = false, unique = true)
    private String storedKey;

    @Column(name = "file_url", length = 1024)
    private String fileUrl;

    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "bucket_type", length = 10, nullable = false)
    private BucketType bucketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FileStatus status;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static File create(
        FileTargetType targetType,
        String originalFileName,
        String storedKey,
        String fileUrl,
        String contentType,
        Long fileSize,
        BucketType bucketType,
        Long uploadedBy
    ) {
        return File.builder()
            .id(Generators.timeBasedEpochGenerator().generate().toString())
            .targetType(targetType)
            .originalFileName(originalFileName)
            .storedKey(storedKey)
            .fileUrl(fileUrl)
            .contentType(contentType)
            .fileSize(fileSize)
            .bucketType(bucketType)
            .status(FileStatus.PENDING)
            .uploadedBy(uploadedBy)
            .build();
    }

    public void attach(String targetId) {
        this.targetId = targetId;
        this.status = FileStatus.ATTACHED;
    }

    public void markDeleted() {
        this.status = FileStatus.DELETED;
    }
}
