package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.WorkspaceImageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceImagesRequestDto;
import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerUpdateWorkspaceImages 테스트")
class ManagerUpdateWorkspaceImagesTest {

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @Mock
    private WorkspaceImageQueryRepository workspaceImageQueryRepository;

    @Mock
    private WorkspaceImageRepository workspaceImageRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileDeleteService fileDeleteService;

    @Mock
    private AttachFilesUseCase attachFiles;

    @InjectMocks
    private ManagerUpdateWorkspaceImages managerUpdateWorkspaceImages;

    @Captor
    private ArgumentCaptor<List<WorkspaceImage>> imagesCaptor;

    @Captor
    private ArgumentCaptor<List<String>> fileIdsCaptor;

    private UpdateWorkspaceImagesRequestDto request(String... fileIds) {
        Set<WorkspaceImageRequestDto> images = new LinkedHashSet<>();
        for (int i = 0; i < fileIds.length; i++) {
            images.add(new WorkspaceImageRequestDto(fileIds[i], i));
        }
        return new UpdateWorkspaceImagesRequestDto(images);
    }

    @Test
    @DisplayName("5개를 초과하면 ILLEGAL_ARGUMENT 예외가 발생한다")
    void execute_5개초과_예외() {
        // given
        ManagerActor actor = mock(ManagerActor.class);

        // when & then
        assertThatThrownBy(() -> managerUpdateWorkspaceImages.execute(
            actor, 1L, request("f1", "f2", "f3", "f4", "f5", "f6")))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        then(workspaceImageRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("존재하지 않는 업장이면 WORKSPACE_NOT_FOUND 예외가 발생한다")
    void execute_업장없음_예외() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        given(workspaceQueryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> managerUpdateWorkspaceImages.execute(actor, 1L, request("f1")))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    @Test
    @DisplayName("다른 매니저의 업장이면 WORKSPACE_NOT_FOUND 예외가 발생한다")
    void execute_다른매니저_예외() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser actorManager = mock(ManagerUser.class);
        given(actor.getManagerUser()).willReturn(actorManager);
        given(actorManager.getId()).willReturn(2L);

        Workspace workspace = mock(Workspace.class);
        ManagerUser owner = mock(ManagerUser.class);
        given(workspace.getManagerUser()).willReturn(owner);
        given(owner.getId()).willReturn(1L);
        given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));

        // when & then
        assertThatThrownBy(() -> managerUpdateWorkspaceImages.execute(actor, 1L, request("f1")))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));
        then(attachFiles).should(never()).execute(any(), any(), any(), any());
    }

    @Test
    @DisplayName("신규 파일은 attach 후 순서대로 WorkspaceImage를 생성한다")
    void execute_신규추가() {
        // given
        ManagerActor actor = mockOwnerActor(1L);
        given(actor.getUserId()).willReturn(100L);

        Workspace workspace = mockOwnerWorkspace(1L);
        given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceImageQueryRepository.findAllByWorkspaceId(1L)).willReturn(List.of());

        // when
        managerUpdateWorkspaceImages.execute(actor, 1L, request("f1", "f2"));

        // then
        then(attachFiles).should().execute(
            fileIdsCaptor.capture(),
            eq(FileTargetType.WORKSPACE_REPRESENTATIVE_IMAGE),
            eq("1"),
            eq(100L)
        );
        assertThat(fileIdsCaptor.getValue()).containsExactly("f1", "f2");

        then(workspaceImageRepository).should().saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue()).hasSize(2);
        assertThat(imagesCaptor.getValue()).extracting(WorkspaceImage::getFileId).containsExactly("f1", "f2");
        assertThat(imagesCaptor.getValue()).extracting(WorkspaceImage::getSortOrder).containsExactly(0, 1);

        then(workspaceImageRepository).should(never()).deleteAll(any());
        then(fileDeleteService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("빠진 이미지는 파일 soft-delete 후 제거하고, 유지 이미지는 순서를 갱신한다")
    void execute_삭제및순서변경() {
        // given
        ManagerActor actor = mockOwnerActor(1L);
        Workspace workspace = mockOwnerWorkspace(1L);
        given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));

        WorkspaceImage imageA = mock(WorkspaceImage.class); // 삭제 대상
        WorkspaceImage imageB = mock(WorkspaceImage.class); // 유지 + 순서 변경
        given(imageA.getFileId()).willReturn("fileA");
        given(imageB.getFileId()).willReturn("fileB");
        given(workspaceImageQueryRepository.findAllByWorkspaceId(1L)).willReturn(List.of(imageA, imageB));

        File fileA = mock(File.class);
        given(fileQueryRepository.findAllByIdIn(List.of("fileA"))).willReturn(List.of(fileA));

        // when: fileB만 유지 (fileA 삭제), fileB는 0번으로 이동
        managerUpdateWorkspaceImages.execute(actor, 1L, request("fileB"));

        // then
        then(fileDeleteService).should().delete(fileA);
        then(workspaceImageRepository).should().deleteAll(List.of(imageA));
        then(imageB).should().updateSortOrder(0);
        then(attachFiles).should(never()).execute(any(), any(), any(), any());
        then(workspaceImageRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("중복 파일 ID는 제거되어 한 번만 반영된다")
    void execute_중복제거() {
        // given
        ManagerActor actor = mockOwnerActor(1L);
        given(actor.getUserId()).willReturn(100L);
        Workspace workspace = mockOwnerWorkspace(1L);
        given(workspaceQueryRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceImageQueryRepository.findAllByWorkspaceId(1L)).willReturn(List.of());

        // when
        managerUpdateWorkspaceImages.execute(actor, 1L, request("f1", "f1", "f2"));

        // then
        then(attachFiles).should().execute(fileIdsCaptor.capture(), any(), any(), any());
        assertThat(fileIdsCaptor.getValue()).containsExactly("f1", "f2");
        then(workspaceImageRepository).should().saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue()).hasSize(2);
    }

    private ManagerActor mockOwnerActor(Long managerId) {
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser actorManager = mock(ManagerUser.class);
        given(actor.getManagerUser()).willReturn(actorManager);
        given(actorManager.getId()).willReturn(managerId);
        return actor;
    }

    private Workspace mockOwnerWorkspace(Long managerId) {
        Workspace workspace = mock(Workspace.class);
        ManagerUser owner = mock(ManagerUser.class);
        given(workspace.getManagerUser()).willReturn(owner);
        given(owner.getId()).willReturn(managerId);
        return workspace;
    }
}
