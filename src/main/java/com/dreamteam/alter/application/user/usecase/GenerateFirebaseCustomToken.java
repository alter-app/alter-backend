package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FirebaseCustomTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.GenerateFirebaseCustomTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("generateFirebaseCustomToken")
@RequiredArgsConstructor
@Transactional
public class GenerateFirebaseCustomToken implements GenerateFirebaseCustomTokenUseCase {

    private final AuthService authService;

    @Override
    public FirebaseCustomTokenResponseDto execute(AppActor actor) {
        String firebaseUid = String.valueOf(actor.getUser().getId());
        String firebaseCustomToken = authService.generateFirebaseCustomToken(firebaseUid);

        return FirebaseCustomTokenResponseDto.of(firebaseCustomToken);
    }
}
