package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceReasonListResponse {

	private Long id;
	private String reason;
	private LocalDateTime createdAt;
}
