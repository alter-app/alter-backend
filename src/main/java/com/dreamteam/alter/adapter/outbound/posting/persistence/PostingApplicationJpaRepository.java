package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingApplicationJpaRepository extends JpaRepository<PostingApplication, Long> {
}
