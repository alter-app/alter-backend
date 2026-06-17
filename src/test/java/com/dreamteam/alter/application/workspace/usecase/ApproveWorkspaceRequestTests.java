package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveWorkspaceRequest 테스트")
class ApproveWorkspaceRequestTests {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private ManagerUserQueryRepository managerUserQueryRepository;

    @Mock
    private ManagerUserRepository managerUserRepository;

    @Mock
    private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

    @Mock
    private WorkspaceImageRepository workspaceImageRepository;

    @Mock
    private WorkspaceRequestImageQueryRepository workspaceRequestImageQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileDeleteService fileDeleteService;

    @InjectMocks
    private ApproveWorkspaceRequest approveWorkspaceRequest;

    @Captor
    private ArgumentCaptor<List<WorkspaceImage>> imagesCaptor;

    private WorkspaceRequest mockApprovedRequest() {
        WorkspaceRequest request = mock(WorkspaceRequest.class);
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(request.getUser()).willReturn(user);
        given(workspaceRequestQueryRepository.findByIdWithUser(10L)).willReturn(Optional.of(request));
        given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.of(mock(ManagerUser.class)));
        return request;
    }

    private WorkspaceRequestImage requestImage(String fileId) {
        WorkspaceRequestImage image = mock(WorkspaceRequestImage.class);
        given(image.getFileId()).willReturn(fileId);
        return image;
    }

    @Test
    @DisplayName("대표이미지가 없으면 WorkspaceImage 저장을 호출하지 않는다")
    void execute_대표이미지없음() {
        // given
        mockApprovedRequest();
        given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(10L)).willReturn(List.of());
        given(fileQueryRepository.findByTargetTypeAndTargetId(any(), any())).willReturn(Optional.empty());

        // when
        approveWorkspaceRequest.execute(10L);

        // then
        then(workspaceImageRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("신청 단계 파일을 업장으로 재첨부하고 순서대로 WorkspaceImage를 생성한다")
    void execute_대표이미지마이그레이션() {
        // given
        mockApprovedRequest();
        WorkspaceRequestImage riA = requestImage("fileA");
        WorkspaceRequestImage riB = requestImage("fileB");
        given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(10L))
            .willReturn(List.of(riA, riB));

        File fileA = mock(File.class);
        File fileB = mock(File.class);
        given(fileA.getId()).willReturn("fileA");
        given(fileB.getId()).willReturn("fileB");
        given(fileQueryRepository.findAllByIdIn(List.of("fileA", "fileB"))).willReturn(List.of(fileA, fileB));
        given(fileQueryRepository.findByTargetTypeAndTargetId(any(), any())).willReturn(Optional.empty());

        // when
        approveWorkspaceRequest.execute(10L);

        // then
        then(fileA).should().attach(anyString());
        then(fileB).should().attach(anyString());
        then(workspaceImageRepository).should().saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue()).extracting(WorkspaceImage::getFileId).containsExactly("fileA", "fileB");
        assertThat(imagesCaptor.getValue()).extracting(WorkspaceImage::getSortOrder).containsExactly(0, 1);
    }

    @Test
    @DisplayName("신청~승인 사이 삭제된 파일은 건너뛰고 존재하는 파일만 마이그레이션한다")
    void execute_일부파일누락() {
        // given
        mockApprovedRequest();
        WorkspaceRequestImage riA = requestImage("fileA");
        WorkspaceRequestImage riB = requestImage("fileB");
        given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(10L))
            .willReturn(List.of(riA, riB));

        File fileA = mock(File.class);
        given(fileA.getId()).willReturn("fileA");
        given(fileQueryRepository.findAllByIdIn(List.of("fileA", "fileB"))).willReturn(List.of(fileA));
        given(fileQueryRepository.findByTargetTypeAndTargetId(any(), any())).willReturn(Optional.empty());

        // when
        approveWorkspaceRequest.execute(10L);

        // then
        then(fileA).should().attach(anyString());
        then(workspaceImageRepository).should().saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue()).extracting(WorkspaceImage::getFileId).containsExactly("fileA");
    }
}
