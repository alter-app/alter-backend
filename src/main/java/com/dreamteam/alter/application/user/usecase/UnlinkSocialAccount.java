package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UnlinkSocialAccountRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.inbound.UnlinkSocialAccountUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service("unlinkSocialAccount")
@RequiredArgsConstructor
@Transactional
public class UnlinkSocialAccount implements UnlinkSocialAccountUseCase {

    private final UserSocialQueryRepository userSocialQueryRepository;
    private final UserSocialRepository userSocialRepository;

    @Override
    public void execute(AppActor actor, UnlinkSocialAccountRequestDto request) {
        User user = actor.getUser();

        UserSocial userSocial = userSocialQueryRepository
            .findByUserIdAndSocialProvider(user.getId(), request.getProvider())
            .orElseThrow(() -> new CustomException(ErrorCode.SOCIAL_ACCOUNT_NOT_LINKED));

        if (ObjectUtils.isEmpty(user.getPassword())) {
            long count = userSocialQueryRepository.countByUserId(user.getId());
            if (count <= 1) {
                throw new CustomException(ErrorCode.SOCIAL_UNLINK_NOT_ALLOWED);
            }
        }

        userSocialRepository.delete(userSocial);
    }
}
