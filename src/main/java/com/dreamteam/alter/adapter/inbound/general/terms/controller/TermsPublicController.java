package com.dreamteam.alter.adapter.inbound.general.terms.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.terms.dto.PublishedTermsItemResponseDto;
import com.dreamteam.alter.domain.terms.port.inbound.GetPublishedTermsListUseCase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/terms")
@RequiredArgsConstructor
@Validated
public class TermsPublicController implements TermsPublicControllerSpec {

    @Resource(name = "getPublishedTermsList")
    private final GetPublishedTermsListUseCase getPublishedTermsList;

    @Override
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<PublishedTermsItemResponseDto>>> getPublishedTermsList() {
        return ResponseEntity.ok(CommonApiResponse.of(getPublishedTermsList.execute()));
    }
}
