package com.dreamteam.alter.common.exception.handler;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingScheduleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("요일 값이 잘못되면 프로젝트 오류 포맷으로 400을 반환한다")
    void 잘못된_요일값_400() throws Exception {
        String body = """
            {"id": 1, "workingDays": ["monday"], "startTime": "09:00", "endTime": "18:00", "position": "홀서빙"}
            """;

        mockMvc.perform(post("/test").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("B001"));
    }

    @Test
    @DisplayName("시각 값이 잘못되면 프로젝트 오류 포맷으로 400을 반환한다")
    void 잘못된_시각값_400() throws Exception {
        String body = """
            {"id": 1, "workingDays": ["MONDAY"], "startTime": "25:00", "endTime": "18:00", "position": "홀서빙"}
            """;

        mockMvc.perform(post("/test").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("B001"));
    }

    @Test
    @DisplayName("업로드 요청 크기 한도를 넘으면 프로젝트 오류 포맷으로 400(B024)을 반환한다")
    void 업로드_크기_초과_400() throws Exception {
        mockMvc.perform(post("/test/upload"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("B024"));
    }

    @RestController
    static class TestController {

        @PostMapping("/test")
        void receive(@RequestBody UpdatePostingScheduleDto request) {
        }

        @PostMapping("/test/upload")
        void upload() {
            throw new MaxUploadSizeExceededException(25L * 1024 * 1024);
        }
    }
}
