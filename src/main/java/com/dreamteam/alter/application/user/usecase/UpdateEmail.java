package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.UpdateEmailUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("updateEmail")
@RequiredArgsConstructor
@Transactional
public class UpdateEmail implements UpdateEmailUseCase {

    private final UserQueryRepository userQueryRepository;
    private final EmailVerificationSessionStoreRepository emailVerificationSessionStoreRepository;

    @Override
    public void execute(AppActor actor, String emailVerificationSessionId) {

        // 이메일 인증 세션 검증
        String verifiedEmail = emailVerificationSessionStoreRepository
                .getEmailBySession(emailVerificationSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "이메일 인증 세션이 유효하지 않거나 만료되었습니다."));

        // 사용자 조회
        User user = userQueryRepository.findById(actor.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 이메일과 동일하면 변경 불필요
        if (verifiedEmail.equals(user.getEmail())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "현재 등록된 이메일과 동일합니다.");
        }

        // 다른 사용자가 이미 사용 중인 이메일인지 확인
        userQueryRepository.findByEmail(verifiedEmail)
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
                });

        user.updateEmail(verifiedEmail);

        // 세션 삭제
        emailVerificationSessionStoreRepository.deleteSession(emailVerificationSessionId);
    }
}
