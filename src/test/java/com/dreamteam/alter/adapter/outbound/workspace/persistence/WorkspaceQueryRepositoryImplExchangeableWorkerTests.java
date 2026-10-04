package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceQueryRepositoryImplExchangeableWorkerTests extends WorkspaceShiftPersistenceTestSupport {

    private static final LocalDateTime START = LocalDateTime.of(2099, 10, 5, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2099, 10, 5, 18, 0);

    @Test
    void 교환_후보_조회는_타_업장_근무와_겹치는_근무자를_제외한다() {
        Workspace workspace = saveWorkspace();
        User requester = saveUser();
        saveWorker(workspace, requester);

        User busyElsewhere = saveUser();
        WorkspaceWorker busyWorker = saveWorker(workspace, busyElsewhere);
        saveConfirmedShift(saveWorker(saveWorkspace(), busyElsewhere), START.plusHours(2), END.plusHours(2));

        WorkspaceWorker freeWorker = saveWorker(workspace, saveUser());

        List<Long> ids = workspaceQueryRepository.getExchangeableWorkerIds(workspace.getId(), requester, START, END);
        long count = workspaceQueryRepository.getExchangeableWorkerCount(workspace.getId(), requester, START, END);
        List<UserWorkspaceWorkerListResponse> page = workspaceQueryRepository.getExchangeableWorkerListWithCursor(
            workspace.getId(), requester, START, END, CursorPageRequest.of((CursorDto) null, 20));

        assertThat(ids).containsExactly(freeWorker.getId()).doesNotContain(busyWorker.getId());
        assertThat(count).isEqualTo(1);
        assertThat(page).extracting(UserWorkspaceWorkerListResponse::getId).containsExactly(freeWorker.getId());
    }
}
