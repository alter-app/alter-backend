package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReasonComment;

@Repository
public interface WorkspaceReasonCommentJpaRepository extends JpaRepository<WorkspaceReasonComment, Long> {
}
