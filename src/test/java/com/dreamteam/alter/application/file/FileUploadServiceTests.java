package com.dreamteam.alter.application.file;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTests {
    @Mock FileRepository files;
    @Mock S3Client s3;
    @InjectMocks FileUploadService upload;

    @Test
    void invalidMimeIsRejectedBeforeS3UploadOrDatabaseSave() {
        MultipartFile file = new MockMultipartFile("file", "invalid.txt", "text/plain", new byte[]{1});
        assertThatThrownBy(() -> upload.upload(file, FileTargetType.WORKSPACE_CERTIFICATE, BucketType.PRIVATE, 1L))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.INVALID_FILE_TYPE);
        verifyNoInteractions(s3, files);
    }

    @Test
    void oversizedFileIsRejectedBeforeS3UploadOrDatabaseSave() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(20L * 1024 * 1024 + 1);
        assertThatThrownBy(() -> upload.upload(file, FileTargetType.WORKSPACE_CERTIFICATE, BucketType.PRIVATE, 1L))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED);
        verifyNoInteractions(s3, files);
    }

    @Test
    void validUploadStoresValidatedMetadataAndOwnerWithPendingStatus() {
        MultipartFile file = new MockMultipartFile("file", "synthetic.pdf", "application/pdf", new byte[]{1, 2, 3});
        when(s3.upload(eq(file), any(), eq(BucketType.PRIVATE))).thenReturn(null);
        when(files.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        String id = upload.upload(file, FileTargetType.WORKSPACE_CERTIFICATE, BucketType.PRIVATE, 1L);
        ArgumentCaptor<File> captured = ArgumentCaptor.forClass(File.class);
        verify(files).save(captured.capture());
        assertThat(captured.getValue().getId()).isEqualTo(id);
        assertThat(captured.getValue().getUploadedBy()).isEqualTo(1L);
        assertThat(captured.getValue().getContentType()).isEqualTo("application/pdf");
        assertThat(captured.getValue().getFileSize()).isEqualTo(3L);
        assertThat(captured.getValue().getStatus()).isEqualTo(FileStatus.PENDING);
        assertThat(captured.getValue().getTargetType()).isEqualTo(FileTargetType.WORKSPACE_CERTIFICATE);
    }
}
