package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRequestResponse {

	private Long id;
	private String businessRegistrationNo;
	private String businessName;
	private String businessType;
	private String businessTypeDetail;
	private String contact;
	private String fullAddress;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private WorkspaceRequestStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
