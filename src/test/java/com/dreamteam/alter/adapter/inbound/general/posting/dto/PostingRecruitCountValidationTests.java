package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class PostingRecruitCountValidationTests {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private String content(Integer recruitCount) {
        return "{\"title\":\"공고\",\"description\":\"설명\",\"payAmount\":12000,\"paymentType\":\"HOURLY\""
            + (recruitCount == null ? "" : ",\"recruitCount\":" + recruitCount);
    }
    private CreatePostingRequestDto create(Integer count) throws Exception {
        return mapper.readValue(content(count) + ",\"workspaceId\":1,\"schedules\":[{\"workingDays\":[\"MONDAY\"],"
            + "\"startTime\":\"09:00\",\"endTime\":\"18:00\",\"position\":\"홀서빙\"}]}", CreatePostingRequestDto.class);
    }
    private UpdatePostingRequestDto update(Integer count) throws Exception {
        return mapper.readValue(content(count) + "}", UpdatePostingRequestDto.class);
    }

    @ParameterizedTest @NullSource @ValueSource(ints = {0, -1})
    void recruitCountMustBePresentAndPositive(Integer count) throws Exception {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(create(count))).extracting(v -> v.getPropertyPath().toString()).contains("recruitCount");
            assertThat(factory.getValidator().validate(update(count))).extracting(v -> v.getPropertyPath().toString()).contains("recruitCount");
        }
    }
    @Test void validRecruitCountMapsToBothCommands() throws Exception {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var create = create(2);
            var update = update(4);
            assertThat(factory.getValidator().validate(create)).isEmpty();
            assertThat(factory.getValidator().validate(update)).isEmpty();
            assertThat(create.toCommand().recruitCount()).isEqualTo(2);
            assertThat(update.toCommand().recruitCount()).isEqualTo(4);
        }
    }
}
