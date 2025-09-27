package com.dreamteam.alter.domain.workspace.port.outbound;

import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;

public interface WorkspaceShiftRepository {
    WorkspaceShift save(WorkspaceShift shift);
    void delete(WorkspaceShift shift);
}
