package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingScheduleDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "positions_needed", nullable = false)
    private int positions_needed;

    @Column(name = "position", nullable = false)
    private Long position;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PostingSchedule create(PostingScheduleDto request, Posting posting) {
        return PostingSchedule.builder()
            .posting(posting)
            .dayOfWeek(request.getDayOfWeek())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .positions_needed(request.getPositionsNeeded())
            .position(request.getPosition())
            .build();
    }

}
