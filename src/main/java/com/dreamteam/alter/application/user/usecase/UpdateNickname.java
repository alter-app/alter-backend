package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.command.UpdateNicknameCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.UpdateNicknameUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("updateNickname")
@RequiredArgsConstructor
@Transactional
public class UpdateNickname implements UpdateNicknameUseCase {

    private final UserQueryRepository userQueryRepository;
    private final EntityManager entityManager;

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

        try {
            user.updateNickname(newNickname);
            entityManager.flush();
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }
    }
}
