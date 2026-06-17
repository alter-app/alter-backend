package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.WorkspaceRequestCommentResponseDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkspaceRequestCommentListResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceRequestCommentListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestCommentQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.CommentOwner;

import lombok.RequiredArgsConstructor;

@Service("getWorkspaceRequestCommentList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceRequestCommentList implements GetWorkspaceRequestCommentListUseCase {

	private final WorkspaceRequestQueryRepository workspaceRequestQueryRepository;
	private final WorkspaceRequestCommentQueryRepository workspaceRequestCommentQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final FileUrlService fileUrlService;

	@Override
	public List<WorkspaceRequestCommentResponseDto> execute(Long workspaceRequestId, User requester, CommentOwner ownerType) {
		if (ownerType == CommentOwner.USER) {
			if (!workspaceRequestQueryRepository.existsByIdAndUserId(workspaceRequestId, requester.getId())) {
				throw new CustomException(ErrorCode.FORBIDDEN);
			}
		} else if (!workspaceRequestQueryRepository.existsById(workspaceRequestId)) {
			throw new CustomException(ErrorCode.NOT_FOUND, "해당 업장 등록 신청을 찾을 수 없습니다.");
		}

		List<WorkspaceRequestCommentListResponse> comments =
			workspaceRequestCommentQueryRepository.getCommentList(workspaceRequestId);

		Map<String, List<FileResponseDto>> filesByCommentId = getFilesByCommentId(comments);

		return comments.stream()
			.map(comment -> WorkspaceRequestCommentResponseDto.from(
				comment,
				filesByCommentId.getOrDefault(comment.getId().toString(), List.of())
			))
			.toList();
	}

	private Map<String, List<FileResponseDto>> getFilesByCommentId(List<WorkspaceRequestCommentListResponse> comments) {
		List<String> commentIds = comments.stream()
			.map(comment -> comment.getId().toString())
			.toList();

		return fileQueryRepository
			.findAllByTargetTypeAndTargetIdIn(FileTargetType.WORKSPACE_REQUEST_COMMENT, commentIds)
			.stream()
			.collect(Collectors.groupingBy(
				File::getTargetId,
				Collectors.mapping(fileUrlService::resolve, Collectors.toList())
			));
	}
}
