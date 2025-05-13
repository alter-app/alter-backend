package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.outbound.PostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class PostingRepositoryImpl implements PostingRepository {

    private final PostingJpaRepository postingJpaRepository;

    @Override
    public Posting save(Posting posting) {
        return postingJpaRepository.save(posting);
    }

}
