package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UpdateUserCertificateRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.UpdateUserSelfCertificateUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("updateUserSelfCertificate")
@RequiredArgsConstructor
@Transactional
public class UpdateUserSelfCertificate implements UpdateUserSelfCertificateUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public void execute(AppActor actor, Long certificateId, UpdateUserCertificateRequestDto request) {
        userQueryRepository.findById(actor.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
            .getCertificates()
            .stream()
            .filter(it -> it.getId().equals(certificateId))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.USER_CERTIFICATE_NOT_FOUND))
            .update(request);
    }

}
