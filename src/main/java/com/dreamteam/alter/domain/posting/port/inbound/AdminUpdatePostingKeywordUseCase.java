package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.domain.posting.command.AdminUpdatePostingKeywordCommand;

public interface AdminUpdatePostingKeywordUseCase {

    void execute(Long id, AdminUpdatePostingKeywordCommand command);
}
