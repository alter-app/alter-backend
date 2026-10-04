package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.WorkspaceImageRequestDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceRequestValidationTests {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() { factory = Validation.buildDefaultValidatorFactory(); validator = factory.getValidator(); }
    @AfterAll
    static void close() { factory.close(); }

    @ParameterizedTest
    @ValueSource(strings = {"bizName", "brn", "address", "province", "district", "town", "businessTypeId",
        "latitude", "longitude", "workspaceCertFileId", "workspaceOwnIdentityFileId"})
    void missingRequiredFieldsAreRejectedByRequestDto(String field) {
        CreateWorkspaceRequestDto dto = request();
        ReflectionTestUtils.setField(dto, field, null);
        assertThat(validator.validate(dto)).extracting(violation -> violation.getPropertyPath().toString()).contains(field);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bizName", "brn", "address", "province", "district", "town", "workspaceCertFileId", "workspaceOwnIdentityFileId"})
    void blankRequiredFieldsAreRejectedByRequestDto(String field) {
        CreateWorkspaceRequestDto dto = request();
        ReflectionTestUtils.setField(dto, field, "   ");
        assertThat(validator.validate(dto)).extracting(violation -> violation.getPropertyPath().toString()).contains(field);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123-45-12345", "1234512345", "1", "letters-123"})
    void existingNonblankBusinessNumberContractIsPreserved(String brn) {
        CreateWorkspaceRequestDto dto = request();
        dto.setBrn(brn);
        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    void representativeImageCountKeepsExistingMaximumFive(int count) {
        CreateWorkspaceRequestDto dto = request();
        Set<WorkspaceImageRequestDto> images = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) images.add(new WorkspaceImageRequestDto("image" + i, i));
        dto.setRepresentativeImages(images);
        if (count == 5) assertThat(validator.validate(dto)).isEmpty();
        else assertThat(validator.validate(dto)).extracting(violation -> violation.getPropertyPath().toString())
            .contains("representativeImages");
    }

    private CreateWorkspaceRequestDto request() {
        return new CreateWorkspaceRequestDto("검증 업장", "123-45-12345", "서울 구로구 고척동", "서울", "구로구", "고척동",
            BigDecimal.ONE, BigDecimal.ONE, 1L, null, "02-1234-5678", "certificate", "identity", null, null);
    }
}
