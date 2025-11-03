package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FirebaseCustomTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.ManagerGenerateFirebaseCustomTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerGenerateFirebaseCustomToken")
@RequiredArgsConstructor
@Transactional
public class ManagerGenerateFirebaseCustomToken implements ManagerGenerateFirebaseCustomTokenUseCase {

    private final AuthService authService;

    @Override
    public FirebaseCustomTokenResponseDto execute(ManagerActor actor) {
        String firebaseUid = String.valueOf(actor.getManagerUser().getId());
        String firebaseCustomToken = authService.generateFirebaseCustomToken(firebaseUid);

        return FirebaseCustomTokenResponseDto.of(firebaseCustomToken);
    }
}
