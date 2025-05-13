package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.domain.posting.entity.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingJpaRepository extends JpaRepository<Posting, Long> {
}
