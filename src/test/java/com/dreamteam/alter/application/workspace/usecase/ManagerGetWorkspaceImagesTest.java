package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceImageResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerGetWorkspaceImages 테스트")
class ManagerGetWorkspaceImagesTest {

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @Mock
    private WorkspaceImageQueryRepository workspaceImageQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

    @InjectMocks
    private ManagerGetWorkspaceImages managerGetWorkspaceImages;

    @Test
    @DisplayName("관리 업장이 아니면 WORKSPACE_NOT_FOUND 예외가 발생한다")
    void execute_관리업장아님_예외() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(actor.getManagerUser()).willReturn(managerUser);
        given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> managerGetWorkspaceImages.execute(actor, 1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    @Test
    @DisplayName("대표이미지가 없으면 빈 목록을 반환한다")
    void execute_이미지없음_빈목록() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(actor.getManagerUser()).willReturn(managerUser);
        given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);
        given(workspaceImageQueryRepository.findAllByWorkspaceId(1L)).willReturn(List.of());

        // when
        List<WorkspaceImageResponseDto> result = managerGetWorkspaceImages.execute(actor, 1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이미지 순서대로 매핑하며 파일이 없는 항목은 건너뛰고 노출 순서를 0부터 재계산한다")
    void execute_정상_순서매핑() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(actor.getManagerUser()).willReturn(managerUser);
        given(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).willReturn(true);

        WorkspaceImage imageA = mock(WorkspaceImage.class);
        WorkspaceImage imageMissing = mock(WorkspaceImage.class);
        WorkspaceImage imageB = mock(WorkspaceImage.class);
        given(imageA.getFileId()).willReturn("file-A");
        given(imageMissing.getFileId()).willReturn("file-missing");
        given(imageB.getFileId()).willReturn("file-B");
        given(workspaceImageQueryRepository.findAllByWorkspaceId(1L))
            .willReturn(List.of(imageA, imageMissing, imageB));

        File fileA = mock(File.class);
        File fileB = mock(File.class);
        given(fileA.getId()).willReturn("file-A");
        given(fileB.getId()).willReturn("file-B");
        given(fileQueryRepository.findAllByIdIn(List.of("file-A", "file-missing", "file-B")))
            .willReturn(List.of(fileA, fileB));

        FileResponseDto dtoA = FileResponseDto.of(fileA, "https://url/A");
        FileResponseDto dtoB = FileResponseDto.of(fileB, "https://url/B");
        given(fileUrlService.resolve(fileA)).willReturn(dtoA);
        given(fileUrlService.resolve(fileB)).willReturn(dtoB);

        // when
        List<WorkspaceImageResponseDto> result = managerGetWorkspaceImages.execute(actor, 1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(WorkspaceImageResponseDto::getFileId)
            .containsExactly("file-A", "file-B");
        assertThat(result).extracting(WorkspaceImageResponseDto::getUrl)
            .containsExactly("https://url/A", "https://url/B");
        assertThat(result).extracting(WorkspaceImageResponseDto::getSortOrder)
            .containsExactly(0, 1);
    }
}
