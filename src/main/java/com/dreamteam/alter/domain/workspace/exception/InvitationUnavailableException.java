package com.dreamteam.alter.domain.workspace.exception;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import lombok.Getter;

import java.util.List;

@Getter
public class InvitationUnavailableException extends CustomException {

    private final List<String> unavailablePhoneNumbers;

    public InvitationUnavailableException(List<String> unavailablePhoneNumbers) {
        super(ErrorCode.ILLEGAL_ARGUMENT, "발송할 수 없는 전화번호가 포함되어 있습니다.");
        this.unavailablePhoneNumbers = unavailablePhoneNumbers;
    }

}
