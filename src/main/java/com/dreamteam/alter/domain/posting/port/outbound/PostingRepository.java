package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.domain.posting.entity.Posting;

public interface PostingRepository {

    Posting save(Posting posting);

}
