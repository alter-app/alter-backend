package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import com.dreamteam.alter.domain.workspace.command.CreateWorkspaceRequestCommand;
import com.dreamteam.alter.adapter.inbound.manager.workspace.controller.ManagerWorkspaceRequestController;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.common.exception.handler.GlobalExceptionHandler;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.port.inbound.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkspaceRequestContactTests {
    private final ObjectMapper mapper = new ObjectMapper();
    private CreateWorkspaceRequestUseCase create;
    private User user;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        create = mock(CreateWorkspaceRequestUseCase.class);
        user = mock(User.class);
        AppActionContext.getInstance().setActor(AppActor.from(user, List.of()));
        ManagerUser manager = mock(ManagerUser.class);
        when(manager.getUser()).thenReturn(user);
        ManagerActionContext.getInstance().setActor(ManagerActor.from(manager, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(new UserWorkspaceRequestController(create,
                mock(GetWorkspaceRequestListUseCase.class), mock(GetWorkspaceRequestUseCase.class),
                mock(CancelWorkspaceRequestUseCase.class), mock(GetBusinessTypeListUseCase.class)),
            new ManagerWorkspaceRequestController(create,
                mock(GetWorkspaceRequestListUseCase.class), mock(GetWorkspaceRequestUseCase.class),
                mock(CancelWorkspaceRequestUseCase.class), mock(GetBusinessTypeListUseCase.class)))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @AfterEach
    void clearContext() {
        AppActionContext.clear();
        ManagerActionContext.clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"010-1234-5678", "02-1234-5678", "031-123-4567", "070-1234-5678", "099-1234-5678", "021234567", "01012345678"})
    void 유효한_원문을_검증한_후_유스케이스에_전달한다(String contact) throws Exception {
        for (String path : List.of("/app/workspace-requests", "/manager/workspace-requests")) {
            clearInvocations(create);
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(body(contact))).andExpect(status().isOk());
            ArgumentCaptor<CreateWorkspaceRequestCommand> captor = ArgumentCaptor.forClass(CreateWorkspaceRequestCommand.class);
            verify(create).execute(captor.capture());
            assertThat(captor.getValue()).isEqualTo(new CreateWorkspaceRequestCommand(user,
                "연락처 테스트", "123-45-12345", "서울 구로구 고척동", "서울", "구로구", "고척동",
                new BigDecimal("37"), new BigDecimal("127"), 1L, "업종 상세", contact,
                "cert", "identity", "warrant", List.of("img-a", "img-b", "img-c")));
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", " 01012345678", "01012345678 ", "01012a45678", "+821012345678",
        "010-12345678", "0101234-5678", "010--1234-5678", "010.1234.5678", "010 1234 5678",
        "010–1234–5678", "０１０１２３４５６７８", "123456789", "01234567", "012345678901",
        "010-12345-6789", "0-1234-5678", "01012345678\n"})
    void 잘못된_원문은_유스케이스_호출_전에_거부한다(String contact) throws Exception {
        for (String path : List.of("/app/workspace-requests", "/manager/workspace-requests")) {
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(body(contact))).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(create);
    }

    private String body(String contact) throws Exception {
        Map<String, Object> body = new HashMap<>(Map.of(
            "bizName", "연락처 테스트", "brn", "123-45-12345", "address", "서울 구로구 고척동",
            "province", "서울", "district", "구로구", "town", "고척동", "latitude", 37,
            "longitude", 127, "businessTypeId", 1));
        body.put("contact", contact);
        body.put("workspaceCertFileId", "cert");
        body.put("workspaceOwnIdentityFileId", "identity");
        body.put("workspaceWarrantFileId", "warrant");
        body.put("businessTypeDetail", "업종 상세");
        body.put("representativeImages", List.of(Map.of("fileId", "img-c"),
            Map.of("fileId", "img-b", "sortOrder", 2), Map.of("fileId", "img-a", "sortOrder", 1),
            Map.of("fileId", "img-a", "sortOrder", 3)));
        return mapper.writeValueAsString(body);
    }
}
