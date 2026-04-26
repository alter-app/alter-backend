package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceReasonComment;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceReasonCommentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceReasonCommentRepositoryImpl implements WorkspaceReasonCommentRepository {

    private final WorkspaceReasonCommentJpaRepository workspaceReasonCommentJpaRepository;

    @Override
    public WorkspaceReasonComment save(WorkspaceReasonComment comment) {
        return workspaceReasonCommentJpaRepository.save(comment);
    }
}
