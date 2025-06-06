package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.domain.posting.entity.PostingApplication;

public interface PostingApplicationRepository {
    void save(PostingApplication postingApplication);
}
