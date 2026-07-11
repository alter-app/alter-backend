package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.domain.posting.command.AdminCreatePostingKeywordCommand;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;

public interface AdminCreatePostingKeywordUseCase {

    PostingKeyword execute(AdminCreatePostingKeywordCommand command);
}
