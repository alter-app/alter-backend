package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingScheduleRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingScheduleDto;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

import java.time.DayOfWeek;
import java.time.LocalTime;
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

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @Column(name = "title", length = 128, nullable = false)
    private String title;

    @Column(name = "description", length = Integer.MAX_VALUE, nullable = false)
    private String description;

    @Column(name = "pay_amount", nullable = false)
    private int payAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", length = 20, nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
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

    public static Posting create(CreatePostingRequestDto request, Workspace workspace, List<PostingKeyword> postingKeywords) {
        Posting posting = Posting.builder()
            .workspace(workspace)
            .title(request.getTitle())
            .description(request.getDescription())
            .payAmount(request.getPayAmount())
            .paymentType(request.getPaymentType())
            .status(PostingStatus.OPEN)
            .build();

        if (ObjectUtils.isNotEmpty(request.getSchedules())) {
            posting.schedules = request.getSchedules()
                .stream()
                .map(scheduleDto -> PostingSchedule.create(
                    scheduleDto.getWorkingDays(),
                    scheduleDto.getStartTime(),
                    scheduleDto.getEndTime(),
                    scheduleDto.getPositionsNeeded(),
                    scheduleDto.getPosition(),
                    posting
                ))
                .toList();
        }

        if (ObjectUtils.isNotEmpty(postingKeywords)) {
            posting.keywords = postingKeywords
                .stream()
                .map(keyword -> PostingKeywordMap.create(keyword, posting))
                .toList();
        }

        return posting;
    }

    public void updateStatus(PostingStatus status) {
        this.status = status;
    }

    public void updateContent(
        String title,
        String description,
        int payAmount,
        PaymentType paymentType,
        List<PostingKeyword> postingKeywords,
        List<CreatePostingScheduleRequestDto> createSchedules,
        List<UpdatePostingScheduleDto> updateSchedules,
        List<Long> deleteScheduleIds
    ) {
        this.title = title;
        this.description = description;
        this.payAmount = payAmount;
        this.paymentType = paymentType;

        // 키워드 업데이트
        this.keywords.clear();
        if (ObjectUtils.isNotEmpty(postingKeywords)) {
            this.keywords = postingKeywords
                .stream()
                .map(keyword -> PostingKeywordMap.create(keyword, this))
                .toList();
        }

        // 스케줄 삭제 처리
        if (ObjectUtils.isNotEmpty(deleteScheduleIds)) {
            for (Long scheduleId : deleteScheduleIds) {
                PostingSchedule existingSchedule = this.schedules.stream()
                    .filter(schedule -> schedule.getId().equals(scheduleId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("삭제할 스케줄을 찾을 수 없습니다: " + scheduleId));
                
                existingSchedule.updateStatus(PostingStatus.DELETED);
            }
        }

        // 스케줄 수정 처리
        if (ObjectUtils.isNotEmpty(updateSchedules)) {
            for (UpdatePostingScheduleDto updateDto : updateSchedules) {
                PostingSchedule existingSchedule = this.schedules.stream()
                    .filter(schedule -> schedule.getId().equals(updateDto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("수정할 스케줄을 찾을 수 없습니다: " + updateDto.getId()));
                
                existingSchedule.update(
                    updateDto.getWorkingDays().stream()
                        .map(DayOfWeek::valueOf)
                        .toList(),
                    LocalTime.parse(updateDto.getStartTime()),
                    LocalTime.parse(updateDto.getEndTime()),
                    updateDto.getPositionsNeeded(),
                    updateDto.getPosition()
                );
            }
        }

        // 스케줄 추가 처리
        if (ObjectUtils.isNotEmpty(createSchedules)) {
            for (CreatePostingScheduleRequestDto createDto : createSchedules) {
                PostingSchedule newSchedule = PostingSchedule.create(
                    createDto.getWorkingDays(),
                    createDto.getStartTime(),
                    createDto.getEndTime(),
                    createDto.getPositionsNeeded(),
                    createDto.getPosition(),
                    this
                );
                this.schedules.add(newSchedule);
            }
        }
    }
}
