package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
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
    public void execute(AppActor actor) {
        User user = actor.getUser();

        if (user.getEmail() == null) {
            throw new CustomException(ErrorCode.EMAIL_NOT_REGISTERED);
        }

        user.removeEmail();
    }
}
