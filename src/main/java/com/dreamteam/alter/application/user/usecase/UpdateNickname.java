package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.command.UpdateNicknameCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.UpdateNicknameUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("updateNickname")
@RequiredArgsConstructor
@Transactional
public class UpdateNickname implements UpdateNicknameUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public void execute(UpdateNicknameCommand command) {
        User user = command.user();
        String newNickname = command.nickname();

        if (user.getNickname().equals(newNickname)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "현재 닉네임과 동일합니다.");
        }

        userQueryRepository.findByNickname(newNickname)
            .ifPresent(existing -> {
                throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
            });

        user.updateNickname(newNickname);
    }
}
