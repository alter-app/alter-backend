package com.dreamteam.alter.domain.workspace.entity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "workspace_worker_schedules")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceWorkerSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_worker_id", nullable = false)
	private WorkspaceWorker workspaceWorker;

	@Enumerated(EnumType.STRING)
	@Column(name = "day_of_week", nullable = false)
	private DayOfWeek dayOfWeek;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private WorkspaceWorkerScheduleStatus status;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public static WorkspaceWorkerSchedule create(
		WorkspaceWorker workspaceWorker,
		DayOfWeek dayOfWeek,
		LocalTime startTime,
		LocalTime endTime
	) {
		WorkspaceWorkerSchedule workspaceWorkerSchedule = WorkspaceWorkerSchedule.builder()
			.workspaceWorker(workspaceWorker)
			.dayOfWeek(dayOfWeek)
			.startTime(startTime)
			.endTime(endTime)
			.status(WorkspaceWorkerScheduleStatus.ACTIVATED)
			.build();

		workspaceWorkerSchedule.validTime();
		return workspaceWorkerSchedule;
	}

	public void update(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;

		validTime();
	}

	public void validTime() {
		if (this.startTime.isAfter(this.endTime))
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "시작 시간은 종료 시간보다 늦을 수 없습니다.");
	}

	public void validOverlappingTime(LocalTime newStart, LocalTime newEnd) {
		if (newStart.isBefore(this.endTime) && newEnd.isAfter(this.startTime))
			throw new CustomException(ErrorCode.CONFLICT, "같은 요일에 겹치는 근무 시간이 존재합니다.");
	}

	public void delete() {
		this.status = WorkspaceWorkerScheduleStatus.DELETED;
	}
}
