package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdatePasswordRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PasswordValidator;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.UpdatePasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("updatePassword")
@RequiredArgsConstructor
@Transactional
public class UpdatePassword implements UpdatePasswordUseCase {

    private final PasswordEncoder passwordEncoder;

    @Override
    public void execute(AppActor actor, UpdatePasswordRequestDto request) {
        User user = actor.getUser();

        if (ObjectUtils.isNotEmpty(user.getPassword())) {
            if (ObjectUtils.isEmpty(request.getCurrentPassword()) ||
                !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
            }
        }

        if (!PasswordValidator.isValid(request.getNewPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
