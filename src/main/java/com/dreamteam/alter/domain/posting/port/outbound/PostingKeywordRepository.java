package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingKeywordRepository extends JpaRepository<PostingKeyword, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
