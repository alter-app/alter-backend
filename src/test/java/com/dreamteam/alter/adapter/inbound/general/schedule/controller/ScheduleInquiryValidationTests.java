package com.dreamteam.alter.adapter.inbound.general.schedule.controller;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.application.workspace.usecase.GetMySchedule;
import com.dreamteam.alter.application.workspace.usecase.GetExchangeableSelfSchedules;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.common.exception.handler.GlobalExceptionHandler;
import com.dreamteam.alter.domain.workspace.port.inbound.GetWorkspaceScheduleUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverters;
import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ScheduleInquiryValidationTests {
    private WorkspaceShiftQueryRepository shifts;
    private GetWorkspaceScheduleUseCase workspace;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setup() {
        shifts = mock(WorkspaceShiftQueryRepository.class);
        workspace = mock(GetWorkspaceScheduleUseCase.class);
        when(shifts.findByUserAndDate(any(), anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
            LocalDate.of((Integer) call.getArgument(1), (Integer) call.getArgument(2), (Integer) call.getArgument(3));
            return List.of();
        });
        when(shifts.findByUserAndDateRange(any(), anyInt(), anyInt())).thenReturn(List.of());
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        AppActionContext.getInstance().setActor(mock(AppActor.class));
        mvc = MockMvcBuilders.standaloneSetup(new UserScheduleController(new GetMySchedule(shifts), workspace))
            .setValidator(validator).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @AfterEach
    void cleanup() {
        validator.close();
        AppActionContext.clear();
    }

    @ParameterizedTest
    @CsvSource({"2026,10,32", "2026,13,1", "2026,2,29", "2026,4,31", "2026,0,1", "2026,1,0"})
    void invalidCompleteDailyDateReturnsBadRequestBeforeQuery(int year, int month, int day) throws Exception {
        mvc.perform(get("/app/schedules/self").param("year", "" + year).param("month", "" + month).param("day", "" + day))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("code").value("B001"));
        verifyNoInteractions(shifts);
    }

    @ParameterizedTest
    @CsvSource({"2024,2,29", "2026,2,28", "2026,4,30", "2026,12,31"})
    void validDailyDateStillReachesExistingQuery(int year, int month, int day) throws Exception {
        mvc.perform(get("/app/schedules/self").param("year", "" + year).param("month", "" + month).param("day", "" + day))
            .andExpect(status().isOk());
        verify(shifts).findByUserAndDate(any(), eq(year), eq(month), eq(day));
    }

    @Test
    void monthOutsideCalendarRangeWithoutDayPreservesEmptyMonthContract() throws Exception {
        mvc.perform(get("/app/schedules/self").param("year", "2026").param("month", "13"))
            .andExpect(status().isOk());
        verify(shifts).findByUserAndDateRange(any(), eq(2026), eq(13));
    }

    @Test
    void incompleteCombinationRetainsExistingUseCaseMessage() throws Exception {
        mvc.perform(get("/app/schedules/self").param("year", "2026"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("message").value("연단위 요청은 불가능합니다."));
        verifyNoInteractions(shifts);
    }

    @Test
    void workspaceEndpointStillPassesIgnoredInvalidDayWithoutValidation() throws Exception {
        mvc.perform(get("/app/schedules/workspaces/1").param("year", "2026").param("month", "10").param("day", "32"))
            .andExpect(status().isOk());
        verify(workspace).execute(any(), eq(1L), eq(new WorkScheduleInquiryRequestDto(2026, 10, 32)));
    }

    @Test
    void requestHasOnlyOriginalExternalPropertiesAndIncompleteDatesAreNotRejected() throws Exception {
        assertThat(new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(
            new WorkScheduleInquiryRequestDto(2026, 10, 1))).properties())
            .extracting(java.util.Map.Entry::getKey).containsExactlyInAnyOrder("year", "month", "day");
        assertThat(ModelConverters.getInstance().read(WorkScheduleInquiryRequestDto.class)
            .get("WorkScheduleInquiryRequestDto").getProperties().keySet())
            .containsExactlyInAnyOrder("year", "month", "day");
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(new WorkScheduleInquiryRequestDto(2026, null, 32))).isEmpty();
            assertThat(factory.getValidator().validate(new WorkScheduleInquiryRequestDto(null, 13, 1))).isEmpty();
        }
    }

    @Test
    void candidateEndpointPreservesIgnoredDayAndItsOwnMissingYearMonthMessage() throws Exception {
        WorkspaceQueryRepository workspaces = mock(WorkspaceQueryRepository.class);
        Workspace existing = mock(Workspace.class);
        when(workspaces.findById(1L)).thenReturn(Optional.of(existing));
        when(workspaces.isUserActiveWorkerInWorkspace(any(), eq(1L))).thenReturn(true);
        when(shifts.findByUserAndWorkspaceAndMonthFrom(any(), eq(existing), eq(2030), eq(1), any()))
            .thenReturn(List.of());
        var controller = new UserSubstituteRequestController(null, null, null, null, null, null, null, null,
            new GetExchangeableSelfSchedules(workspaces, shifts));
        MockMvc candidateMvc = MockMvcBuilders.standaloneSetup(controller).setValidator(validator)
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        candidateMvc.perform(get("/app/workspaces/1/exchangeable-schedules")
            .param("year", "2030").param("month", "1").param("day", "32"))
            .andExpect(status().isOk());
        verify(shifts).findByUserAndWorkspaceAndMonthFrom(any(), eq(existing), eq(2030), eq(1), any());
        candidateMvc.perform(get("/app/workspaces/1/exchangeable-schedules").param("year", "2030"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("message").value("근무일정 조회 시 연도, 월 파라미터는 필수입니다."));
    }
}
