package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileValidatorTests {
    private static final long LIMIT = 20L * 1024 * 1024;

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"})
    void allExistingMimeTypesAllowExactly20MiB(String contentType) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(LIMIT);
        when(file.getContentType()).thenReturn(contentType);
        assertThatCode(() -> FileValidator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    void oneByteAbove20MiBIsRejected() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(LIMIT + 1);
        assertThatThrownBy(() -> FileValidator.validate(file)).isInstanceOf(CustomException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"text/plain", "application/zip", "image/svg+xml"})
    void unsupportedOrMissingMimeTypeIsRejected(String contentType) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(contentType);
        assertThatThrownBy(() -> FileValidator.validate(file)).isInstanceOf(CustomException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INVALID_FILE_TYPE);
    }

    @Test
    void missingAndEmptyFilesAreRejected() {
        assertThatThrownBy(() -> FileValidator.validate(null)).isInstanceOf(CustomException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INVALID_FILE);
        assertThatThrownBy(() -> FileValidator.validate(new MockMultipartFile("file", new byte[0])))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.INVALID_FILE);
    }
}
