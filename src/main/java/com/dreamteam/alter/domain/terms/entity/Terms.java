package com.dreamteam.alter.domain.terms.entity;

import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "terms")
@DynamicUpdate
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private TermsType type;

    @Column(name = "version", length = 20, nullable = false)
    private String version;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "notion_url", length = 1000, nullable = false)
    private String notionUrl;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TermsStatus status;

    @Column(name = "effective_at", nullable = true)
    private LocalDateTime effectiveAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Terms create(TermsType type, String version, String title, String notionUrl, boolean required) {
        return Terms.builder()
                .type(type)
                .version(version)
                .title(title)
                .notionUrl(notionUrl)
                .required(required)
                .status(TermsStatus.DRAFT)
                .effectiveAt(null)
                .build();
    }

    public void update(String title, String notionUrl, boolean required) {
        this.title = title;
        this.notionUrl = notionUrl;
        this.required = required;
    }

    public void publish() {
        this.status = TermsStatus.PUBLISHED;
        this.effectiveAt = LocalDateTime.now();
    }

    public void deprecate() {
        this.status = TermsStatus.DEPRECATED;
    }

    public void delete() {
        this.status = TermsStatus.DELETED;
    }
}
