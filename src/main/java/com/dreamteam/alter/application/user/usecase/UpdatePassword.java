package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PasswordValidator;
import com.dreamteam.alter.domain.user.command.UpdatePasswordCommand;
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
    public void execute(UpdatePasswordCommand command) {
        User user = command.user();

        if (ObjectUtils.isNotEmpty(user.getPassword())) {
            if (ObjectUtils.isEmpty(command.currentPassword()) ||
                !passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
            }
        }

        if (ObjectUtils.isNotEmpty(command.currentPassword()) &&
            command.currentPassword().equals(command.newPassword())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        if (!PasswordValidator.isValid(command.newPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        user.updatePassword(passwordEncoder.encode(command.newPassword()));
    }
}
