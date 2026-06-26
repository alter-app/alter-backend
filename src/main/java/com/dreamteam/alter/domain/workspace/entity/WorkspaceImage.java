package com.dreamteam.alter.domain.workspace.entity;

import java.time.LocalDateTime;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "workspace_images")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @Column(name = "file_id", length = 36, nullable = false)
    private String fileId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WorkspaceImage create(Workspace workspace, String fileId, int sortOrder) {
        validateSortOrder(sortOrder);
        return WorkspaceImage.builder()
            .workspace(workspace)
            .fileId(fileId)
            .sortOrder(sortOrder)
            .build();
    }

    public void updateSortOrder(int sortOrder) {
        validateSortOrder(sortOrder);
        this.sortOrder = sortOrder;
    }

    private static void validateSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "노출 순서는 0 이상이어야 합니다.");
        }
    }
}
