package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckEmailDuplicationRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckEmailDuplicationResponseDto;
import com.dreamteam.alter.domain.user.port.inbound.CheckEmailDuplicationUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service("checkEmailDuplication")
@RequiredArgsConstructor
@Slf4j
public class CheckEmailDuplication implements CheckEmailDuplicationUseCase {
    
    private final UserQueryRepository userQueryRepository;
    
    @Override
    public CheckEmailDuplicationResponseDto execute(CheckEmailDuplicationRequestDto request) {
        boolean isDuplicated = ObjectUtils.isNotEmpty(userQueryRepository.findByEmail(request.getEmail()));

        return CheckEmailDuplicationResponseDto.of(request.getEmail(), isDuplicated);
    }
}
