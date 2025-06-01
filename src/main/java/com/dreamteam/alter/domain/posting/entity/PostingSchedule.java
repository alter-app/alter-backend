package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingScheduleRequestDto;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    @Type(JsonBinaryType.class)
    @Column(name = "working_days", nullable = false, columnDefinition = "jsonb")
    private List<DayOfWeek> workingDays;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "positions_needed", nullable = false)
    private int positionsNeeded;

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

    public static PostingSchedule create(CreatePostingScheduleRequestDto request, Posting posting) {
        return PostingSchedule.builder()
            .posting(posting)
            .workingDays(request.getWorkingDays())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .positionsNeeded(request.getPositionsNeeded())
            .position(request.getPosition())
            .status(PostingStatus.OPEN)
            .build();
    }

}
