package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "postings")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Posting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspace;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "pay_amount", nullable = false)
    private int payAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostingStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "posting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostingSchedule> schedules;

    @OneToMany(mappedBy = "posting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostingKeywordMap> keywords;

    public static Posting create(CreatePostingRequestDto request, List<Keyword> keywords) {
        Posting posting = Posting.builder()
            .workspace(request.getWorkspaceId())
            .title(request.getTitle())
            .description(request.getDescription())
            .payAmount(request.getPayAmount())
            .paymentType(request.getPaymentType())
            .status(PostingStatus.OPEN)
            .build();

        if (ObjectUtils.isNotEmpty(request.getSchedules())) {
            posting.schedules = request.getSchedules()
                .stream()
                .map(scheduleDto -> PostingSchedule.create(scheduleDto, posting))
                .toList();
        }

        if (ObjectUtils.isNotEmpty(keywords)) {
            posting.keywords = keywords
                .stream()
                .map(keyword -> PostingKeywordMap.create(keyword, posting))
                .toList();
        }

        return posting;
    }

}
