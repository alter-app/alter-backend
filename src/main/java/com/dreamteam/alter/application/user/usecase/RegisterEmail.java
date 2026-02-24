package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.RegisterEmailUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("registerEmail")
@RequiredArgsConstructor
@Transactional
public class RegisterEmail implements RegisterEmailUseCase {

    private final UserQueryRepository userQueryRepository;
    private final EmailVerificationSessionStoreRepository emailVerificationSessionStoreRepository;

    @Override
    public void execute(AppActor actor, String emailVerificationSessionId) {

        // 이메일 인증 세션 검증
        String verifiedEmail = emailVerificationSessionStoreRepository
                .getEmailBySession(emailVerificationSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "이메일 인증 세션이 유효하지 않거나 만료되었습니다."));

        // 이메일 중복 확인
        if (userQueryRepository.findByEmail(verifiedEmail).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 사용자 조회 및 이메일 등록
        User user = userQueryRepository.findById(actor.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.registerEmail(verifiedEmail);

        // 세션 삭제
        emailVerificationSessionStoreRepository.deleteSession(emailVerificationSessionId);
    }
}
