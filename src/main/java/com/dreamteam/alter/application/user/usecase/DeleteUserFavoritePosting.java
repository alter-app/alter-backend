package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.DeleteUserFavoritePostingUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserFavoritePostingQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserFavoritePostingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("deleteUserFavoritePosting")
@RequiredArgsConstructor
@Transactional
public class DeleteUserFavoritePosting implements DeleteUserFavoritePostingUseCase {

    private final UserFavoritePostingQueryRepository userFavoritePostingQueryRepository;
    private final UserFavoritePostingRepository userFavoritePostingRepository;

    @Override
    public void execute(AppActor actor, Long postingId) {
        userFavoritePostingRepository.delete(
            userFavoritePostingQueryRepository.findByPostingIdAndUser(postingId, actor.getUser())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_FAVORITE_POSTING_NOT_FOUND)));
    }

}
