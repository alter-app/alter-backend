package com.dreamteam.alter.adapter.inbound.general.schedule.controller;

import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.exception.handler.GlobalExceptionHandler;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.command.GetReceivedSubstituteRequestDetailCommand;
import com.dreamteam.alter.domain.workspace.port.inbound.GetReceivedSubstituteRequestDetailUseCase;
import com.dreamteam.alter.domain.workspace.result.ReceivedSubstituteRequestDetailResult;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ReceivedSubstituteRequestDetailControllerTests {

    private final User user = mock(User.class);
    private final GetReceivedSubstituteRequestDetailUseCase useCase = mock(GetReceivedSubstituteRequestDetailUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        UserSubstituteRequestController controller = mock(UserSubstituteRequestController.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(controller, "getReceivedSubstituteRequestDetailUseCase", useCase);
        AppActionContext.getInstance().setActor(AppActor.from(user, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @AfterEach
    void clearContext() {
        AppActionContext.clear();
    }

    @Test
    void receivedDetailHasADirectRoute() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 10, 12, 9, 0);
        when(useCase.execute(new GetReceivedSubstituteRequestDetailCommand(user, 1L))).thenReturn(new ReceivedSubstituteRequestDetailResult(
            1L, SubstituteRequestStatus.CANCELLED, SubstituteRequestTargetStatus.CANCELLED,
            2L, start, start.plusHours(3), "홀", 3L, "업장", 4L, "요청자", null));
        mvc
            .perform(get("/app/users/me/substitute-requests/received/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status.value").value("CANCELLED"))
            .andExpect(jsonPath("$.data.myTargetStatus.value").value("CANCELLED"))
            .andExpect(jsonPath("$.data.schedule.scheduleId").value(2))
            .andExpect(jsonPath("$.data.workspace.workspaceId").value(3))
            .andExpect(jsonPath("$.data.requester.workerId").value(4))
            .andExpect(jsonPath("$.data.targets").doesNotExist())
            .andExpect(jsonPath("$.data.acceptedWorker").doesNotExist())
            .andExpect(jsonPath("$.data.allowedActions").doesNotExist());
        verify(useCase).execute(new GetReceivedSubstituteRequestDetailCommand(user, 1L));
    }

    @Test
    void missingOrUnauthorizedRequestUsesExistingNotFoundContract() throws Exception {
        when(useCase.execute(new GetReceivedSubstituteRequestDetailCommand(user, 9L)))
            .thenThrow(new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 대타 요청입니다."));
        mvc.perform(get("/app/users/me/substitute-requests/received/9"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("B019"));
    }

    @Test
    void malformedIdUsesExistingBadRequestContract() throws Exception {
        mvc.perform(get("/app/users/me/substitute-requests/received/not-a-number"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("B001"));
    }
}
