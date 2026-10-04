package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "posting_schedules")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class PostingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @ElementCollection
    @CollectionTable(name = "posting_schedule_working_days",
        joinColumns = @JoinColumn(name = "posting_schedule_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private Set<DayOfWeek> workingDays;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "position", length = 128, nullable = false)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PostingStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PostingSchedule create(
        List<DayOfWeek> workingDays,
        LocalTime startTime,
        LocalTime endTime,
        String position,
        Posting posting
    ) {
        validatePeriod(startTime, endTime);
        return PostingSchedule.builder()
            .posting(posting)
            .workingDays(new HashSet<>(workingDays))
            .startTime(startTime)
            .endTime(endTime)
            .position(position)
            .status(PostingStatus.OPEN)
            .build();
    }

    public void update(
        List<DayOfWeek> workingDays,
        LocalTime startTime,
        LocalTime endTime,
        String position
    ) {
        validatePeriod(startTime, endTime);
        this.workingDays.clear();
        this.workingDays.addAll(workingDays);
        this.startTime = startTime;
        this.endTime = endTime;
        this.position = position;
    }

    public void updateStatus(PostingStatus status) {
        this.status = status;
    }

    public List<DayOfWeek> getWorkingDays() {
        return workingDays.stream().sorted().toList();
    }

    private static void validatePeriod(LocalTime startTime, LocalTime endTime) {
        if (startTime.equals(endTime)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "근무 시작 시간과 종료 시간은 같을 수 없습니다.");
        }
    }

}
