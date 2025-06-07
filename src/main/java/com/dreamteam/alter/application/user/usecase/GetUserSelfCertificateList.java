package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfCertificateListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfCertificateListUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("getUserSelfCertificateList")
@RequiredArgsConstructor
@Transactional
public class GetUserSelfCertificateList implements GetUserSelfCertificateListUseCase {

    @Override
    public List<UserSelfCertificateListResponseDto> execute(AppActor actor) {
        return actor.getUser()
            .getCertificates()
            .stream()
            .map(UserSelfCertificateListResponseDto::from)
            .toList();
    }

}
