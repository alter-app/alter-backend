package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import java.time.LocalDateTime;

import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRequestListResponse {

	private Long id;
	private String businessName;
	private String fullAddress;
	private LocalDateTime createdAt;
	private WorkspaceRequestStatus status;
}
