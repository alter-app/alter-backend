package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckNicknameDuplicationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckNicknameDuplicationResponseDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserQueryRepositoryImpl;
import com.dreamteam.alter.domain.user.port.inbound.CheckNicknameDuplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service("checkNicknameDuplication")
@RequiredArgsConstructor
public class CheckNicknameDuplication implements CheckNicknameDuplicationUseCase {

    private final UserQueryRepositoryImpl userQueryRepository;

    @Override
    public CheckNicknameDuplicationResponseDto execute(CheckNicknameDuplicationRequestDto request) {

        boolean isDuplicated = ObjectUtils.isNotEmpty(userQueryRepository.findByNickname(request.getNickname()));

        return CheckNicknameDuplicationResponseDto.of(request.getNickname(), isDuplicated);
    }

}
