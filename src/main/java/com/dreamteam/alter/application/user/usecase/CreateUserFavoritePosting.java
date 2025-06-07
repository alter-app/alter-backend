package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.UserFavoritePosting;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserFavoritePostingUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserFavoritePostingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service("createUserFavoritePosting")
@RequiredArgsConstructor
@Transactional
public class CreateUserFavoritePosting implements CreateUserFavoritePostingUseCase {

    private final UserFavoritePostingRepository userFavoritePostingRepository;
    private final PostingQueryRepository postingQueryRepository;

    @Override
    public void execute(AppActor actor, Long postingId) {
        try {
            userFavoritePostingRepository.save(UserFavoritePosting.create(
                actor.getUser(),
                postingQueryRepository.findById(postingId)
                    .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND))
            ));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.USER_FAVORITE_POSTING_DUPLICATED);
        }
    }

}
