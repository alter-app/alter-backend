package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkspaceShiftRepositoryImpl implements WorkspaceShiftRepository {

    private final WorkspaceShiftJpaRepository workspaceShiftJpaRepository;

    @Override
    public WorkspaceShift save(WorkspaceShift shift) {
        return workspaceShiftJpaRepository.save(shift);
    }

    @Override
    public List<WorkspaceShift> saveAll(List<WorkspaceShift> shifts) {
        return workspaceShiftJpaRepository.saveAll(shifts);
    }

    @Override
    public void delete(WorkspaceShift shift) {
        workspaceShiftJpaRepository.delete(shift);
    }
}
