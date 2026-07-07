package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.domain.posting.command.AdminCreatePostingKeywordCommand;

public interface AdminCreatePostingKeywordUseCase {

    Long execute(AdminCreatePostingKeywordCommand command);
}
