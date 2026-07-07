package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;

import java.util.List;

public interface AdminGetPostingKeywordListUseCase {

    List<PostingKeyword> execute();
}
