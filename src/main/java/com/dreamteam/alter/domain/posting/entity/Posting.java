package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.command.PostingScheduleCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.command.UpdatePostingScheduleCommand;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.workspace.entity.Workspace;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public static Posting create(CreatePostingCommand command, Workspace workspace) {
        Posting posting = Posting.builder()
            .workspace(workspace)
            .title(command.title())
            .description(command.description())
            .payAmount(command.payAmount())
            .paymentType(command.paymentType())
            .status(PostingStatus.OPEN)
            .build();

        posting.schedules = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(command.schedules())) {
            posting.addSchedules(command.schedules());
        }

        return posting;
    }

    public void updateStatus(PostingStatus status) {
        this.status = status;
    }

    /**
     * 삭제되지 않은 근무일정만 반환한다. (응답 노출 기준)
     */
    public List<PostingSchedule> getActiveSchedules() {
        if (ObjectUtils.isEmpty(this.schedules)) {
            return List.of();
        }

        return this.schedules.stream()
            .filter(schedule -> !PostingStatus.DELETED.equals(schedule.getStatus()))
            .toList();
    }

    public void updateContent(UpdatePostingCommand command) {
        this.title = command.title();
        this.description = command.description();
        this.payAmount = command.payAmount();
        this.paymentType = command.paymentType();

        // 스케줄 삭제 처리
        if (ObjectUtils.isNotEmpty(command.deleteScheduleIds()))
            deleteSchedules(command.deleteScheduleIds());

        // 스케줄 수정 처리
        if (ObjectUtils.isNotEmpty(command.updateSchedules()))
            updateSchedules(command.updateSchedules());

        // 스케줄 추가 처리
        if (ObjectUtils.isNotEmpty(command.createSchedules()))
            addSchedules(command.createSchedules());
    }

    /**
     * 스케줄 추가
     * @param createSchedules 스케줄 추가 정보 List
     */
    public void addSchedules(List<PostingScheduleCommand> createSchedules) {
        for (PostingScheduleCommand createCommand : createSchedules) {
            PostingSchedule newSchedule = PostingSchedule.create(
                createCommand.workingDays(),
                createCommand.startTime(),
                createCommand.endTime(),
                createCommand.positionsNeeded(),
                createCommand.position(),
                this
            );
            this.schedules.add(newSchedule);
        }
    }

    /**
     * 스케줄 수정
     * @param updateSchedules 스케줄 수정 정보 List
     */
    public void updateSchedules(List<UpdatePostingScheduleCommand> updateSchedules) {
        for (UpdatePostingScheduleCommand updateCommand : updateSchedules) {
            PostingSchedule existingSchedule = getActiveSchedules().stream()
                .filter(schedule -> schedule.getId().equals(updateCommand.id()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "수정할 스케줄을 찾을 수 없습니다."));

            existingSchedule.update(
                updateCommand.workingDays(),
                updateCommand.startTime(),
                updateCommand.endTime(),
                updateCommand.positionsNeeded(),
                updateCommand.position()
            );
        }
    }

    /**
     * 스케줄 삭제 (Soft Delete)
     * @param deleteScheduleIds 삭제할 스케줄 Id List
     */
    public void deleteSchedules(List<Long> deleteScheduleIds) {
        for (Long scheduleId : deleteScheduleIds) {
            PostingSchedule existingSchedule = getActiveSchedules().stream()
                .filter(schedule -> schedule.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "삭제할 스케줄을 찾을 수 없습니다."));

            existingSchedule.updateStatus(PostingStatus.DELETED);
        }
    }
}
