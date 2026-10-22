package com.dreamteam.alter.domain.workspace.entity;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleStatus;
import com.dreamteam.alter.domain.workspace.util.FixedWorkerScheduleCalculator;

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
	@Column(name = "start_day_of_week", length = 16, nullable = false)
	private DayOfWeek startDayOfWeek;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "end_day_of_week", length = 16, nullable = false)
	private DayOfWeek endDayOfWeek;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "recurrence_type", length = 16, nullable = false)
	private WorkspaceWorkerScheduleRecurrenceType recurrenceType;

	@Column(name = "week_of_month")
	private Integer weekOfMonth;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 32, nullable = false)
	private WorkspaceWorkerScheduleStatus status;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public static WorkspaceWorkerSchedule create(
		WorkspaceWorker workspaceWorker,
		DayOfWeek startDayOfWeek,
		LocalTime startTime,
		DayOfWeek endDayOfWeek,
		LocalTime endTime
	) {
		return create(workspaceWorker, new FixedWorkerScheduleCommand(
			startDayOfWeek, startTime, endDayOfWeek, endTime, null, null, null, null
		));
	}

	public static WorkspaceWorkerSchedule create(WorkspaceWorker worker, FixedWorkerScheduleCommand command) {
		if (command.recurrenceType() == null
			&& (command.weekOfMonth() != null || command.startDate() != null || command.endDate() != null)) {
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "반복 방식과 적용 기간을 함께 입력해야 합니다.");
		}
		WorkspaceWorkerSchedule workspaceWorkerSchedule = WorkspaceWorkerSchedule.builder()
			.workspaceWorker(worker)
			.startDayOfWeek(command.startDayOfWeek())
			.startTime(command.startTime())
			.endDayOfWeek(command.endDayOfWeek())
			.endTime(command.endTime())
			.recurrenceType(command.recurrenceType() == null ? WorkspaceWorkerScheduleRecurrenceType.WEEKLY : command.recurrenceType())
			.weekOfMonth(command.weekOfMonth())
			.startDate(command.startDate())
			.endDate(command.endDate())
			.status(WorkspaceWorkerScheduleStatus.ACTIVATED)
			.build();

		workspaceWorkerSchedule.validTime();
		workspaceWorkerSchedule.validRecurrence(command.recurrenceType() != null);
		return workspaceWorkerSchedule;
	}

	public WorkspaceWorkerSchedule updatedSchedule(FixedWorkerScheduleCommand command) {
		if (command.recurrenceType() == null) {
			if (command.weekOfMonth() != null || command.startDate() != null || command.endDate() != null) {
				throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "반복 방식과 적용 기간을 함께 입력해야 합니다.");
			}
			WorkspaceWorkerSchedule candidate = create(workspaceWorker, new FixedWorkerScheduleCommand(
				command.startDayOfWeek(), command.startTime(), command.endDayOfWeek(), command.endTime(),
				null, null, null, null
			));
			candidate.recurrenceType = recurrenceType;
			candidate.weekOfMonth = weekOfMonth;
			candidate.startDate = startDate;
			candidate.endDate = endDate;
			return candidate;
		}
		return create(workspaceWorker, command);
	}

	public void update(FixedWorkerScheduleCommand command) {
		WorkspaceWorkerSchedule candidate = updatedSchedule(command);
		this.startDayOfWeek = candidate.startDayOfWeek;
		this.startTime = candidate.startTime;
		this.endDayOfWeek = candidate.endDayOfWeek;
		this.endTime = candidate.endTime;
		this.recurrenceType = candidate.recurrenceType;
		this.weekOfMonth = candidate.weekOfMonth;
		this.startDate = candidate.startDate;
		this.endDate = candidate.endDate;
	}

	public void validTime() {
		int dayOffset = Math.floorMod(endDayOfWeek.getValue() - startDayOfWeek.getValue(), 7);
		Duration duration = Duration.between(startTime, endTime).plusDays(dayOffset);
		if (duration.isZero() || duration.isNegative() || duration.compareTo(Duration.ofDays(7)) >= 0)
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "근무 시간은 0시간 초과, 7일 미만이어야 합니다.");
	}

	public void validOverlappingTime(WorkspaceWorkerSchedule candidate) {
		if (FixedWorkerScheduleCalculator.overlaps(this, candidate))
			throw new CustomException(ErrorCode.CONFLICT, "겹치는 근무 시간이 존재합니다.");
	}

	private void validRecurrence(boolean explicitRecurrence) {
		if (explicitRecurrence && startDate == null)
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "적용 시작일은 필수입니다.");
		if (endDate != null && (startDate == null || endDate.isBefore(startDate)))
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "적용 종료일은 시작일보다 빠를 수 없습니다.");
		if (recurrenceType == WorkspaceWorkerScheduleRecurrenceType.MONTHLY) {
			if (weekOfMonth == null || weekOfMonth < 1 || weekOfMonth > 5)
				throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "매월 반복은 1~5번째 요일을 지정해야 합니다.");
		} else if (weekOfMonth != null) {
			throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "매주 반복에는 월별 주차를 지정할 수 없습니다.");
		}
	}

	public void delete() {
		this.status = WorkspaceWorkerScheduleStatus.DELETED;
	}
}
