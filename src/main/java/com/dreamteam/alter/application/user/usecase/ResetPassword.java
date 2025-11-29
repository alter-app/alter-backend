package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.ResetPasswordRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.ResetPasswordUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("resetPassword")
@RequiredArgsConstructor
@Transactional
public class ResetPassword implements ResetPasswordUseCase {

    private static final String SESSION_KEY_PREFIX = "PASSWORD_RESET:PENDING:";
    private static final String USER_INDEX_KEY_PREFIX = "PASSWORD_RESET:USER:";

    private final StringRedisTemplate redisTemplate;
    private final UserQueryRepository userQueryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void execute(ResetPasswordRequestDto request) {
        // 세션 확인
        String sessionKey = SESSION_KEY_PREFIX + request.getSessionId();
        String userId = redisTemplate.opsForValue().get(sessionKey);

        if (ObjectUtils.isEmpty(userId)) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_SESSION_NOT_EXIST);
        }

        // 사용자 조회
        User user = userQueryRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        // 세션 키와 인덱스 키 모두 삭제
        redisTemplate.delete(sessionKey);
        String userIndexKey = USER_INDEX_KEY_PREFIX + userId;
        redisTemplate.delete(userIndexKey);
    }
}
