package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import com.dreamteam.alter.application.user.usecase.LinkSocialAccount;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/users/social")
@RequiredArgsConstructor
public class UserSocialController implements UserSocialControllerSpec {

    private final LinkSocialAccount linkSocialAccount;

    @Override
    @PostMapping("/link")
    public ResponseEntity<CommonApiResponse<Void>> linkSocialAccount(
        @Valid @RequestBody LinkSocialAccountRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        
        linkSocialAccount.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
