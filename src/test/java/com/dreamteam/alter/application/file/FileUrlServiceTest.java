package com.dreamteam.alter.application.file;

import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.port.outbound.PresignedUrlCacheRepository;
import com.dreamteam.alter.domain.file.port.outbound.S3Client;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileUrlService 테스트")
class FileUrlServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private PresignedUrlCacheRepository presignedUrlCacheRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @InjectMocks
    private FileUrlService sut;

    private File profileFile(FileTargetType targetType, String targetId, String url) {
        File file = File.create(
            targetType, "profile.png", "stored/" + targetId + "-" + url,
            url, "image/png", 1024L, BucketType.PUBLIC, 1L
        );
        file.attach(targetId);
        return file;
    }

    private File privateProfileFile(String targetId) {
        File file = File.create(
            FileTargetType.USER_PROFILE, "profile.png", "stored/" + java.util.UUID.randomUUID(),
            null, "image/png", 1024L, BucketType.PRIVATE, 1L
        );
        file.attach(targetId);
        return file;
    }

    @Test
    @DisplayName("대상별로 ATTACHED 파일이 여러 건이면 각 그룹의 마지막(최신) 파일의 URL을 반환한다 (ADR-014)")
    void resolveLatestUrlsByTarget_대상별_마지막파일_URL반환() {
        // given
        File oldFileA = profileFile(FileTargetType.USER_PROFILE, "1", "https://cdn.example.com/a-old.png");
        File newFileA = profileFile(FileTargetType.USER_PROFILE, "1", "https://cdn.example.com/a-new.png");
        File fileB = profileFile(FileTargetType.USER_PROFILE, "2", "https://cdn.example.com/b.png");

        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, List.of("1", "2")))
            .willReturn(List.of(oldFileA, newFileA, fileB));

        // when
        Map<String, String> result = sut.resolveLatestUrlsByTarget(FileTargetType.USER_PROFILE, List.of("1", "2"));

        // then
        assertThat(result)
            .containsEntry("1", "https://cdn.example.com/a-new.png")
            .containsEntry("2", "https://cdn.example.com/b.png");
    }

    @Test
    @DisplayName("presigned URL 변환(resolve)은 파일 건수가 아니라 대상 건수만큼만 호출된다 (ALT-281 회귀 방지)")
    void resolveLatestUrlsByTarget_resolve는_대상수만큼만_호출() {
        // given: 대상 1(PRIVATE 파일 3건) + 대상 2(PRIVATE 파일 1건) = 파일 총 4건, 대상은 2개
        File oldFileA = privateProfileFile("1");
        File midFileA = privateProfileFile("1");
        File newFileA = privateProfileFile("1");
        File fileB = privateProfileFile("2");

        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, List.of("1", "2")))
            .willReturn(List.of(oldFileA, midFileA, newFileA, fileB));
        given(presignedUrlCacheRepository.findByFileId(any()))
            .willReturn(java.util.Optional.of(new PresignedUrlResult("https://cdn.example.com/signed", Instant.now())));

        // when
        sut.resolveLatestUrlsByTarget(FileTargetType.USER_PROFILE, List.of("1", "2"));

        // then: presigned URL 변환은 대상별로 선택된 최신 파일(2건)에 대해서만 일어나야 한다 (파일 4건 전부가 아니라)
        verify(presignedUrlCacheRepository, times(2)).findByFileId(any());
    }

    @Test
    @DisplayName("단건 편의 메서드는 배치 메서드에 원소 1개로 위임한다")
    void resolveLatestUrlByTarget_단건은_배치에_위임() {
        // given
        File file = profileFile(FileTargetType.USER_PROFILE, "1", "https://cdn.example.com/a.png");
        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, List.of("1")))
            .willReturn(List.of(file));

        // when
        String url = sut.resolveLatestUrlByTarget(FileTargetType.USER_PROFILE, "1");

        // then
        assertThat(url).isEqualTo("https://cdn.example.com/a.png");
    }

    @Test
    @DisplayName("대상에 ATTACHED 파일이 없으면 단건 조회는 null을 반환한다")
    void resolveLatestUrlByTarget_파일없으면_null() {
        // given
        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(any(), any()))
            .willReturn(List.of());

        // when
        String url = sut.resolveLatestUrlByTarget(FileTargetType.USER_PROFILE, "999");

        // then
        assertThat(url).isNull();
    }
}
