package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.DeleteUserSelfCertificateUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("deleteUserSelfCertificate")
@RequiredArgsConstructor
@Transactional
public class DeleteUserSelfCertificate implements DeleteUserSelfCertificateUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public void execute(AppActor actor, Long certificateId) {
        userQueryRepository.findById(actor.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
            .getCertificates()
            .stream()
            .filter(it -> it.getId().equals(certificateId))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.USER_CERTIFICATE_NOT_FOUND))
            .delete();
    }

}
