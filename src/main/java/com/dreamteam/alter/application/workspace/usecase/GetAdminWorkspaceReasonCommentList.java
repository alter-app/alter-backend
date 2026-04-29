package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.workspace.dto.AdminWorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceReasonCommentListResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.workspace.port.inbound.GetAdminWorkspaceReasonCommentListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getAdminWorkspaceReasonCommentList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdminWorkspaceReasonCommentList implements GetAdminWorkspaceReasonCommentListUseCase {

	private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;
	private final WorkspaceReasonCommentQueryRepository workspaceReasonCommentQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileUrlService fileUrlService;

	@Override
	public List<AdminWorkspaceReasonCommentResponseDto> execute(Long workspaceRequestId, Long reasonId) {
		workspaceReasonQueryRepository.findByIdAndWorkspaceRequestId(reasonId, workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

		List<WorkspaceReasonCommentListResponse> comments =
			workspaceReasonCommentQueryRepository.getWorkspaceReasonCommentList(reasonId);
		Map<String, List<FileResponseDto>> filesByCommentId = getFilesByCommentId(comments);

		return comments
			.stream()
			.map(comment -> AdminWorkspaceReasonCommentResponseDto.from(
				comment,
				filesByCommentId.getOrDefault(comment.getId().toString(), List.of())
			))
			.toList();
	}

	private Map<String, List<FileResponseDto>> getFilesByCommentId(List<WorkspaceReasonCommentListResponse> comments) {
		List<String> commentIds = comments.stream()
			.map(comment -> comment.getId().toString())
			.toList();

		return fileQueryRepository
			.findAllByTargetTypeAndTargetIdIn(FileTargetType.WORKSPACE_REASON_COMMENT, commentIds)
			.stream()
			.collect(Collectors.groupingBy(
				file -> file.getTargetId(),
				Collectors.mapping(fileUrlService::resolve, Collectors.toList())
			));
	}
}
