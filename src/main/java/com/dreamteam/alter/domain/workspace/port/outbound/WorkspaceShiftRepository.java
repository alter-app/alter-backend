package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;

public interface WorkspaceShiftRepository {
    WorkspaceShift save(WorkspaceShift shift);
    List<WorkspaceShift> saveAll(List<WorkspaceShift> shifts);
    void delete(WorkspaceShift shift);
}
