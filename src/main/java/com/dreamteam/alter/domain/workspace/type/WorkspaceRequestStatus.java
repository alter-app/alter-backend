package com.dreamteam.alter.domain.workspace.type;

import java.util.Map;

public enum WorkspaceRequestStatus {
	PENDING,
	ACTIVATED,
	REVOKED,
	CANCELED,
	;


	public static Map<WorkspaceRequestStatus, String> describe() {
		return Map.of(
			WorkspaceRequestStatus.PENDING, "승인 대기",
			WorkspaceRequestStatus.REVOKED, "반려",
			WorkspaceRequestStatus.ACTIVATED, "활성화",
			WorkspaceRequestStatus.CANCELED, "취소"
		);
	}
}
