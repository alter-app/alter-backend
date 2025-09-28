package com.dreamteam.alter.domain.report.entity;

import com.dreamteam.alter.domain.report.type.ReporterType;
import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "reports")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporter_type", length = 20, nullable = false)
    private ReporterType reporterType;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20, nullable = false)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReportStatus status;

    @Column(name = "admin_comment", length = 500)
    private String adminComment;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Report create(
        ReporterType reporterType,
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        String reason
    ) {
        return Report.builder()
            .reporterType(reporterType)
            .reporterId(reporterId)
            .targetType(targetType)
            .targetId(targetId)
            .reason(reason)
            .status(ReportStatus.PENDING)
            .build();
    }


    public void updateStatus(ReportStatus status, String adminComment) {
        this.status = status;
        this.adminComment = adminComment;
    }

    public void cancel() {
        this.status = ReportStatus.CANCELLED;
    }

    public void delete() {
        this.status = ReportStatus.DELETED;
    }
}
