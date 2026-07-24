package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "business_types")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BusinessType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 64, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255, nullable = true)
    private String description;

    // '기타' 등 상세 입력을 요구하는 업종 식별 플래그. 시드로만 생성되며 관리자 CRUD로는 항상 false.
    @Column(name = "requires_detail", nullable = false)
    private boolean requiresDetail;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BusinessType create(String name, String description) {
        return BusinessType.builder()
            .name(name)
            .description(description)
            .requiresDetail(false)
            .build();
    }

    public void update(String name, String description) {
        if (!this.name.equals(name)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "업종 이름은 변경할 수 없습니다.");
        }
        this.description = description;
    }

    // '기타' 업종만 상세 입력을 요구·저장하고, 그 외 업종의 상세 입력은 무시한다.
    public String resolveDetail(String rawDetail) {
        if (!requiresDetail) {
            return null;
        }
        if (StringUtils.isBlank(rawDetail)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "'기타' 업종은 상세 입력이 필요합니다.");
        }
        return StringUtils.trim(rawDetail);
    }

}
