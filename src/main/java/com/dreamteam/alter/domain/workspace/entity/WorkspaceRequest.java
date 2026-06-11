package com.dreamteam.alter.domain.workspace.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;

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
@Table(name = "workspace_requests")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "user_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private WorkspaceRequestStatus status;

	@Column(name = "business_registration_no", length = 50, nullable = false)
	private String businessRegistrationNo;

	@Column(name = "business_name", length = 128, nullable = false)
	private String businessName;

	@Column(name = "owner_name", length = 64, nullable = false)
	private String ownerName;

	@Column(name = "business_type", length = 128, nullable = false)
	private String businessType;

	@Column(name = "contact", length = 13, nullable = false)
	private String contact;

	@Column(name = "full_address", length = Integer.MAX_VALUE, nullable = false)
	private String fullAddress;

	@Column(name = "province", length = 64, nullable = false)
	private String province;

	@Column(name = "district", length = 64, nullable = false)
	private String district;

	@Column(name = "town", length = 64, nullable = false)
	private String town;

	@Column(name = "latitude", precision = 9, scale = 6, nullable = false)
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 9, scale = 6, nullable = false)
	private BigDecimal longitude;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public static WorkspaceRequest create(
		User user,
		String brn,
		String bizName,
		String ownerName,
		String type,
		String contact,
		String address,
		String province,
		String district,
		String town,
		BigDecimal latitude,
		BigDecimal longitude
	) {
		return WorkspaceRequest.builder()
			.user(user)
			.status(WorkspaceRequestStatus.PENDING)
			.businessRegistrationNo(brn)
			.businessName(bizName)
			.ownerName(ownerName)
			.businessType(type)
			.contact(contact)
			.fullAddress(address)
			.province(province)
			.district(district)
			.town(town)
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}

	public void approve() {
		if (WorkspaceRequestStatus.ACTIVATED.equals(status)) {
			throw new CustomException(ErrorCode.CONFLICT, "이미 승인된 요청입니다.");
		}

		this.status = WorkspaceRequestStatus.ACTIVATED;
	}

	public void reject() {
		this.status = WorkspaceRequestStatus.REVOKED;
	}

	public void cancel() {
		if (!WorkspaceRequestStatus.PENDING.equals(status) && !WorkspaceRequestStatus.REVOKED.equals(status)) {
			throw new CustomException(ErrorCode.CONFLICT, "취소할 수 없는 상태의 요청입니다.");
		}

		this.status = WorkspaceRequestStatus.CANCELED;
	}
}
