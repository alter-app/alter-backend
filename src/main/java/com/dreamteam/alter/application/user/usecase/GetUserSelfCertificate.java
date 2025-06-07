package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfCertificateResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfCertificateUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("getUserSelfCertificate")
@RequiredArgsConstructor
@Transactional
public class GetUserSelfCertificate implements GetUserSelfCertificateUseCase {

    @Override
    public UserSelfCertificateResponseDto execute(AppActor actor, Long certificateId) {
        return UserSelfCertificateResponseDto.from(actor.getUser().getCertificates()
            .stream()
            .filter(it -> it.getId().equals(certificateId))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.USER_CERTIFICATE_NOT_FOUND)));
    }

}
