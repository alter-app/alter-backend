package com.dreamteam.alter.adapter.inbound.manager.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.ManagerSelfInfoResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.GetManagerSelfInfoUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/me")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerSelfController implements ManagerSelfControllerSpec {

    @Resource(name = "getManagerSelfInfo")
    private final GetManagerSelfInfoUseCase getManagerSelfInfo;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<ManagerSelfInfoResponseDto>> getManagerSelfInfo() {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();

        return ResponseEntity.ok(CommonApiResponse.of(getManagerSelfInfo.execute(actor)));
    }
}
