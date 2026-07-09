package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.file.FileDeleteService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserRepository;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceImage;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateWorkspaceRequestStatus 테스트")
class UpdateWorkspaceRequestStatusTests {

    @Mock
    private WorkspaceRequestQueryRepository workspaceRequestQueryRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private ManagerUserQueryRepository managerUserQueryRepository;

    @Mock
    private ManagerUserRepository managerUserRepository;

    @Mock
    private WorkspaceImageRepository workspaceImageRepository;

    @Mock
    private WorkspaceRequestImageQueryRepository workspaceRequestImageQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileDeleteService fileDeleteService;

    @Mock
    private SyncWorkspaceChatMembershipUseCase syncWorkspaceChatMembership;

    @InjectMocks
    private UpdateWorkspaceRequestStatus updateWorkspaceRequestStatus;

    @Captor
    private ArgumentCaptor<List<WorkspaceImage>> imagesCaptor;

    @Captor
    private ArgumentCaptor<Workspace> workspaceCaptor;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("PENDING 으로 변경 요청하면 ILLEGAL_ARGUMENT 예외가 발생하고 신청을 조회하지 않는다")
        void execute_PENDING_예외발생() {
            // when & then
            assertThatThrownBy(() -> updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.PENDING))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
            then(workspaceRequestQueryRepository).should(never()).findByIdWithUser(any());
        }

        @Test
        @DisplayName("CANCELLED 로 변경 요청하면 ILLEGAL_ARGUMENT 예외가 발생한다")
        void execute_CANCELLED_예외발생() {
            // when & then
            assertThatThrownBy(() -> updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.CANCELLED))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        }

        @Test
        @DisplayName("존재하지 않는 신청이면 NOT_FOUND 예외가 발생한다")
        void execute_존재하지않음_예외발생() {
            // given
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
            then(workspaceRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("ACTIVATED 면 기존 ManagerUser 를 재사용해 업장을 생성하고 신분증 파일을 삭제한다")
        void execute_ACTIVATED_기존매니저_승인() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            ManagerUser managerUser = mock(ManagerUser.class);
            User managerUnderlyingUser = mock(User.class);
            File identityFile = mock(File.class);
            given(request.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(managerUser.getUser()).willReturn(managerUnderlyingUser);
            given(managerUnderlyingUser.getId()).willReturn(2L);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.of(managerUser));
            given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(1L)).willReturn(List.of());
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, "1"))
                .willReturn(Optional.of(identityFile));

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED);

            // then
            then(request).should().approve();
            then(managerUserRepository).should(never()).save(any());
            then(workspaceRepository).should().save(any(Workspace.class));
            then(fileDeleteService).should().delete(identityFile);
        }

        @Test
        @DisplayName("ACTIVATED 인데 ManagerUser 가 없으면 새로 생성해 저장한다")
        void execute_ACTIVATED_신규매니저_승인() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            ManagerUser newManagerUser = mock(ManagerUser.class);
            given(request.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(newManagerUser.getUser()).willReturn(user);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(managerUserRepository.save(any(ManagerUser.class))).willReturn(newManagerUser);
            given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(1L)).willReturn(List.of());
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, "1"))
                .willReturn(Optional.empty());

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED);

            // then
            then(request).should().approve();
            then(managerUserRepository).should().save(any(ManagerUser.class));
            then(workspaceRepository).should().save(any(Workspace.class));
            then(fileDeleteService).should(never()).delete(any());
        }

        @Test
        @DisplayName("승인 시 신청의 업종 FK 와 상세를 업장으로 복사한다")
        void execute_ACTIVATED_업종복사() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            BusinessType businessType = mock(BusinessType.class);
            given(request.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(request.getBusinessType()).willReturn(businessType);
            given(request.getBusinessTypeDetail()).willReturn("떡볶이 전문점");
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.of(mock(ManagerUser.class)));
            given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(1L)).willReturn(List.of());
            given(fileQueryRepository.findByTargetTypeAndTargetId(any(), any())).willReturn(Optional.empty());

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED);

            // then
            then(workspaceRepository).should().save(workspaceCaptor.capture());
            assertThat(workspaceCaptor.getValue().getBusinessType()).isEqualTo(businessType);
            assertThat(workspaceCaptor.getValue().getBusinessTypeDetail()).isEqualTo("떡볶이 전문점");
        }

        @Test
        @DisplayName("ACTIVATED 면 업장 저장 후 그룹 채팅방을 생성하고 매니저를 그룹방에 입장시킨다")
        void execute_ACTIVATED_그룹채팅방생성및매니저입장() {
            // given
            User user = mock(User.class);
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            ManagerUser managerUser = mock(ManagerUser.class);
            User managerUnderlyingUser = mock(User.class);
            given(request.getUser()).willReturn(user);
            given(user.getId()).willReturn(1L);
            given(managerUser.getUser()).willReturn(managerUnderlyingUser);
            given(managerUnderlyingUser.getId()).willReturn(42L);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));
            given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.of(managerUser));
            given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(1L)).willReturn(List.of());
            given(fileQueryRepository.findByTargetTypeAndTargetId(FileTargetType.WORKSPACE_OWN_IDENTITY, "1"))
                .willReturn(Optional.empty());

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.ACTIVATED);

            // then
            InOrder inOrder = inOrder(workspaceRepository, syncWorkspaceChatMembership);
            inOrder.verify(workspaceRepository).save(any(Workspace.class));
            inOrder.verify(syncWorkspaceChatMembership).createGroupRoom(any());
            // 참여자 id는 ManagerUser 엔티티 id가 아닌, 채팅 도메인 기준 식별자인 User(계정) id 여야 한다
            inOrder.verify(syncWorkspaceChatMembership).join(any(), eq(42L), eq(TokenScope.MANAGER));
        }

        @Test
        @DisplayName("REVOKED 면 reject 만 처리하고 업장/매니저는 생성하지 않는다")
        void execute_REVOKED_반려() {
            // given
            WorkspaceRequest request = mock(WorkspaceRequest.class);
            given(workspaceRequestQueryRepository.findByIdWithUser(1L)).willReturn(Optional.of(request));

            // when
            updateWorkspaceRequestStatus.execute(1L, WorkspaceRequestStatus.REVOKED);

            // then
            then(request).should().reject();
            then(workspaceRepository).should(never()).save(any());
            then(managerUserRepository).should(never()).save(any());
            then(fileDeleteService).should(never()).delete(any());
            then(syncWorkspaceChatMembership).should(never()).createGroupRoom(any());
            then(syncWorkspaceChatMembership).should(never()).join(any(), any(), any());
        }

        @Test
        @DisplayName("대표이미지가 없으면 WorkspaceImage 저장을 호출하지 않는다")
        void execute_대표이미지없음() {
            // given
            mockApprovedRequest();
            given(workspaceRequestImageQueryRepository.findAllByWorkspaceRequestId(10L)).willReturn(List.of());
            given(fileQueryRepository.findByTargetTypeAndTargetId(any(), any())).willReturn(Optional.empty());

            // when
            updateWorkspaceRequestStatus.execute(10L, WorkspaceRequestStatus.ACTIVATED);

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
            updateWorkspaceRequestStatus.execute(10L, WorkspaceRequestStatus.ACTIVATED);

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
            updateWorkspaceRequestStatus.execute(10L, WorkspaceRequestStatus.ACTIVATED);

            // then
            then(fileA).should().attach(anyString());
            then(workspaceImageRepository).should().saveAll(imagesCaptor.capture());
            assertThat(imagesCaptor.getValue()).extracting(WorkspaceImage::getFileId).containsExactly("fileA");
        }
    }

    private WorkspaceRequest mockApprovedRequest() {
        WorkspaceRequest request = mock(WorkspaceRequest.class);
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(request.getUser()).willReturn(user);
        given(workspaceRequestQueryRepository.findByIdWithUser(10L)).willReturn(Optional.of(request));
        ManagerUser managerUser = mock(ManagerUser.class);
        User managerUnderlyingUser = mock(User.class);
        given(managerUser.getUser()).willReturn(managerUnderlyingUser);
        given(managerUnderlyingUser.getId()).willReturn(2L);
        given(managerUserQueryRepository.findByUserId(1L)).willReturn(Optional.of(managerUser));
        return request;
    }

    private WorkspaceRequestImage requestImage(String fileId) {
        WorkspaceRequestImage image = mock(WorkspaceRequestImage.class);
        given(image.getFileId()).willReturn(fileId);
        return image;
    }
}
