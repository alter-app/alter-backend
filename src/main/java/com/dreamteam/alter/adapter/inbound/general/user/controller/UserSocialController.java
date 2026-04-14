package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UnlinkSocialAccountRequestDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.LinkSocialAccountUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UnlinkSocialAccountUseCase;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/users/social")
@RequiredArgsConstructor
public class UserSocialController implements UserSocialControllerSpec {

    @Resource(name = "linkSocialAccount")
    private final LinkSocialAccountUseCase linkSocialAccount;

    @Resource(name = "unlinkSocialAccount")
    private final UnlinkSocialAccountUseCase unlinkSocialAccount;

    @Override
    @PostMapping("/link")
    public ResponseEntity<CommonApiResponse<Void>> linkSocialAccount(
        @Valid @RequestBody LinkSocialAccountRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        linkSocialAccount.execute(actor, request);
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/unlink/{provider}")
    public ResponseEntity<CommonApiResponse<Void>> unlinkSocialAccount(
        @PathVariable SocialProvider provider
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        unlinkSocialAccount.execute(actor, new UnlinkSocialAccountRequestDto(provider));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }
}
