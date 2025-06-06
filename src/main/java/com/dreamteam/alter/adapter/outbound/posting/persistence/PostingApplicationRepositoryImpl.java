package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostingApplicationRepositoryImpl implements PostingApplicationRepository {

    private final PostingApplicationJpaRepository postingApplicationJpaRepository;

    @Override
    public void save(PostingApplication postingApplication) {
        postingApplicationJpaRepository.save(postingApplication);
    }

}
