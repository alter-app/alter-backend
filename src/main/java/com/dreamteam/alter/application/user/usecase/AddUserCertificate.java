package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserCertificateRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserCertificate;
import com.dreamteam.alter.domain.user.port.inbound.AddUserCertificateUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("addUserCertificate")
@RequiredArgsConstructor
@Transactional
public class AddUserCertificate implements AddUserCertificateUseCase {

    private final UserRepository userRepository;

    @Override
    public void execute(CreateUserCertificateRequestDto request, AppActor actor) {
        User user = actor.getUser();
        user.addCertificate(UserCertificate.create(request, user));

        // 영속성에 관리되지 않으므로 직접 저장 처리
        userRepository.save(user);
    }

}
