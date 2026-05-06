package com.dreamteam.alter.application.user.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.WithdrawalUserUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserCertificateRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequestTarget;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("withdrawalUser")
@RequiredArgsConstructor
@Transactional
public class WithdrawalUser implements WithdrawalUserUseCase {

	private final UserRepository userRepository;
	private final UserCertificateRepository userCertificateRepository;
	private final WorkspaceQueryRepository workspaceQueryRepository;
	private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
	private final WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
	private final SubstituteRequestQueryRepository substituteRequestQueryRepository;
	private final PostingApplicationQueryRepository postingApplicationQueryRepository;
	private final FileQueryRepository fileQueryRepository;
	private final AuthService authService;
	private final NotificationService notificationService;

	@Override
	public void execute(User user) {
		// (매니저) 활성 업장 보유시 차단
		if (workspaceQueryRepository.existsActiveWorkspaceByUserId(user.getId())) {
			throw new CustomException(ErrorCode.CONFLICT, "활성화된 업장이 있습니다.");
		}

		user.withdraw();
		userRepository.save(user);
		userCertificateRepository.deleteAll(user);

		// 활성화된 근무지 퇴직처리
		workspaceWorkerQueryRepository.findAllActiveByUserId(user.getId())
			.forEach(WorkspaceWorker::resign);

		// 고정근무 스케줄 삭제
		workspaceWorkerScheduleQueryRepository.findAllActivatedByUserId(user.getId())
			.forEach(WorkspaceWorkerSchedule::delete);

		// 본인이 요청자인 활성 대타 요청 취소 (자식 target 까지 cascade)
		substituteRequestQueryRepository.findAllActiveByRequesterUserId(user.getId())
			.forEach(SubstituteRequest::cancel);

		// 본인이 PENDING target 으로 지정된 대타 요청 target 행 취소
		substituteRequestQueryRepository.findAllPendingTargetsByUserId(user.getId())
			.forEach(SubstituteRequestTarget::cancel);

		// 본인이 지원한 활성 PostingApplication 취소
		postingApplicationQueryRepository.findAllActiveByUserId(user.getId())
			.forEach(application -> application.updateStatus(PostingApplicationStatus.CANCELLED));

		// 프로필 이미지 삭제
		fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.USER_PROFILE, user.getId().toString())
			.ifPresent(File::markDeleted);

		authService.revokeAllExistingAuthorizations(user);		// 인가 정보 삭제
		notificationService.removeUserDeviceToken(user);		// 디바이스 토큰 삭제
	}
}
