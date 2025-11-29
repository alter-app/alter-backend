package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.FindEmailRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.FindEmailResponseDto;
import com.dreamteam.alter.common.util.MaskUtil;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.FindEmailByContactUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("findEmailByContact")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindEmailByContact implements FindEmailByContactUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public FindEmailResponseDto execute(FindEmailRequestDto request) {
        User user = userQueryRepository.findByContact(request.getContact())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String maskedEmail = MaskUtil.maskEmail(user.getEmail());

        return FindEmailResponseDto.of(maskedEmail);
    }
}
