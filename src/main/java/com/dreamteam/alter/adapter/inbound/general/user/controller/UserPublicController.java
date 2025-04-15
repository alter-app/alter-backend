package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.*;
import com.dreamteam.alter.domain.user.port.inbound.CheckNicknameDuplicationUseCase;
import com.dreamteam.alter.domain.user.port.inbound.CreateUserUseCase;
import com.dreamteam.alter.domain.user.port.inbound.GenerateTokenUseCase;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/users")
@RequiredArgsConstructor
@Validated
public class UserPublicController implements UserPublicControllerSpec {

    @Resource(name = "generateToken")
    private final GenerateTokenUseCase generateToken;

    @Resource(name = "createUser")
    private final CreateUserUseCase createUser;

    @Resource(name = "checkNicknameDuplication")
    private final CheckNicknameDuplicationUseCase checkNicknameDuplication;

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginUser(@RequestBody @Valid LoginUserRequestDto request) {
        return ResponseEntity.ok(CommonApiResponse.of(generateToken.execute(request)));
    }

    @Override
    @PostMapping("/signup")
    public ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> createUser(@RequestBody @Valid CreateUserRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonApiResponse.of(createUser.execute(request)));
    }

    @Override
    @PostMapping("/exists/nickname")
    public ResponseEntity<CommonApiResponse<CheckNicknameDuplicationResponseDto>> checkNicknameDuplication(
        @RequestBody @Valid CheckNicknameDuplicationRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(checkNicknameDuplication.execute(request)));
    }

}
