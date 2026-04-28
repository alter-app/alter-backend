package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceReasonCommentResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceReasonCommentListResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceReasonCommentsUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getWorkspaceReasonComments")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceReasonComments implements GetWorkspaceReasonCommentsUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceReasonQueryRepository workspaceReasonQueryRepository;
	private final WorkspaceReasonCommentQueryRepository workspaceReasonCommentQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileUrlService fileUrlService;

	@Override
	public List<WorkspaceReasonCommentResponseDto> execute(User user, Long workspaceRequestId, Long reasonId) {
		if (!workspaceRequestQueryRepository.existsByIdAndUserId(workspaceRequestId, user.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		workspaceReasonQueryRepository.findByIdAndWorkspaceRequestId(reasonId, workspaceRequestId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

		List<WorkspaceReasonCommentListResponse> comments =
			workspaceReasonCommentQueryRepository.getWorkspaceReasonCommentList(reasonId);
		Map<String, List<FileResponseDto>> filesByCommentId = getFilesByCommentId(comments);

		return comments
			.stream()
			.map(comment -> WorkspaceReasonCommentResponseDto.from(
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
				File::getTargetId,
				Collectors.mapping(fileUrlService::resolve, Collectors.toList())
			));
	}
}
