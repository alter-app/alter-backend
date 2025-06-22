package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckContactDuplicationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckContactDuplicationResponseDto;
import com.dreamteam.alter.domain.user.port.inbound.CheckContactDuplicationUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service("checkContactDuplication")
@RequiredArgsConstructor
public class CheckContactDuplication implements CheckContactDuplicationUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public CheckContactDuplicationResponseDto execute(CheckContactDuplicationRequestDto request) {

        boolean isDuplicated = ObjectUtils.isNotEmpty(userQueryRepository.findByContact(request.getContact()));

        return CheckContactDuplicationResponseDto.of(request.getContact(), isDuplicated);
    }

}
