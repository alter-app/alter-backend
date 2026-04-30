package com.dreamteam.alter.adapter.inbound.manager.social.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.mapper.LinkSocialAccountCommandMapper;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.command.UnlinkSocialAccountCommand;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.GetLinkedSocialAccountsUseCase;
import com.dreamteam.alter.domain.user.port.inbound.LinkSocialAccountUseCase;
import com.dreamteam.alter.domain.user.port.inbound.UnlinkSocialAccountUseCase;
import com.dreamteam.alter.domain.user.port.inbound.dto.SocialAccountStatusDto;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager/social")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerSocialController implements ManagerSocialControllerSpec {

    @Resource(name = "linkSocialAccount")
    private final LinkSocialAccountUseCase linkSocialAccount;

    @Resource(name = "unlinkSocialAccount")
    private final UnlinkSocialAccountUseCase unlinkSocialAccount;

    @Resource(name = "getLinkedSocialAccounts")
    private final GetLinkedSocialAccountsUseCase getLinkedSocialAccounts;

    @Override
    @PostMapping("/link")
    public ResponseEntity<CommonApiResponse<Void>> linkSocialAccount(
        @Valid @RequestBody LinkSocialAccountRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        linkSocialAccount.execute(LinkSocialAccountCommandMapper.toCommand(actor.getManagerUser().getUser(), request));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @DeleteMapping("/unlink/{provider}")
    public ResponseEntity<CommonApiResponse<Void>> unlinkSocialAccount(
        @PathVariable SocialProvider provider
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        unlinkSocialAccount.execute(UnlinkSocialAccountCommand.from(actor.getManagerUser().getUser(), provider));
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/status")
    public ResponseEntity<CommonApiResponse<List<SocialAccountStatusDto>>> getLinkedSocialAccounts() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getLinkedSocialAccounts.execute(actor.getManagerUser().getUser())));
    }
}
