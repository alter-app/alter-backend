package com.dreamteam.alter.adapter.inbound.general.address.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.address.dto.StageAddressRequestDto;
import com.dreamteam.alter.adapter.inbound.general.address.dto.StageAddressResponseDto;
import com.dreamteam.alter.domain.address.port.inbound.GetStageAddressesUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/addresses")
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
public class AddressController {

    @Resource(name = "getStageAddresses")
    private final GetStageAddressesUseCase getStageAddressesUseCase;

    @GetMapping
    public ResponseEntity<CommonApiResponse<StageAddressResponseDto>> getStageAddresses(
        StageAddressRequestDto request
    ) {
        return ResponseEntity.ok(CommonApiResponse.of(
                StageAddressResponseDto.from(getStageAddressesUseCase.execute(request))
            )
        );
    }
}
