package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.command.RemoveEmailCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.RemoveEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("removeEmail")
@RequiredArgsConstructor
@Transactional
public class RemoveEmail implements RemoveEmailUseCase {

    @Override
    public void execute(RemoveEmailCommand command) {
        User user = command.user();

        if (user.getEmail() == null) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "이메일이 등록되지 않은 사용자입니다.");
        }

        user.removeEmail();
    }
}
