package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.WorkspaceImageRequestDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.CreateWorkspaceRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequest;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceRequestImage;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestImageRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceRequestRepository;
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
@DisplayName("CreateWorkspaceRequest 테스트")
class CreateWorkspaceRequestTests {

    @Mock
    private WorkspaceRequestRepository workspaceRequestRepository;

    @Mock
    private WorkspaceRequestImageRepository workspaceRequestImageRepository;

    @Mock
    private BusinessTypeRepository businessTypeRepository;

    @Mock
    private AttachFilesUseCase attachFiles;

    @InjectMocks
    private CreateWorkspaceRequest createWorkspaceRequest;

    @Captor
    private ArgumentCaptor<List<String>> fileIdsCaptor;

    @Captor
    private ArgumentCaptor<List<WorkspaceRequestImage>> imagesCaptor;

    @Captor
    private ArgumentCaptor<WorkspaceRequest> workspaceRequestCaptor;

    private CreateWorkspaceRequestDto baseRequest() {
        CreateWorkspaceRequestDto dto = new CreateWorkspaceRequestDto();
        dto.setBizName("세븐일레븐");
        dto.setBrn("123-45-12345");
        dto.setAddress("서울특별시 구로구 고척동 123");
        dto.setProvince("서울특별시");
        dto.setDistrict("구로구");
        dto.setTown("고척동");
        dto.setBusinessTypeId(1L);
        dto.setContact("02-1234-5678");
        dto.setWorkspaceCertFileId("cert-file");
        dto.setWorkspaceOwnIdentityFileId("identity-file");
        return dto;
    }

    private BusinessType businessType(boolean requiresDetail) {
        BusinessType businessType = mock(BusinessType.class);
        given(businessType.isRequiresDetail()).willReturn(requiresDetail);
        return businessType;
    }

    @Test
    @DisplayName("대표이미지 없이도 정상 처리되며 대표이미지 저장은 호출되지 않는다")
    void execute_대표이미지없음() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(100L);
        BusinessType businessType = businessType(false);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
        given(workspaceRequestRepository.save(any())).willReturn(1L);

        // when
        createWorkspaceRequest.execute(user, baseRequest());

        // then
        then(attachFiles).should().executeMap(any(), eq("1"), eq(100L));
        then(attachFiles).should(never()).execute(any(), any(), any(), any());
        then(workspaceRequestImageRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("중복 대표이미지 ID는 제거되어 순서대로 저장된다")
    void execute_대표이미지중복제거() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(100L);
        BusinessType businessType = businessType(false);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
        given(workspaceRequestRepository.save(any())).willReturn(1L);

        CreateWorkspaceRequestDto dto = baseRequest();
        Set<WorkspaceImageRequestDto> images = new LinkedHashSet<>();
        images.add(new WorkspaceImageRequestDto("img1", 0));
        images.add(new WorkspaceImageRequestDto("img1", 1)); // 동일 fileId → 무시
        images.add(new WorkspaceImageRequestDto("img2", 2));
        dto.setRepresentativeImages(images);

        // when
        createWorkspaceRequest.execute(user, dto);

        // then
        then(attachFiles).should().execute(
            fileIdsCaptor.capture(),
            eq(FileTargetType.WORKSPACE_REPRESENTATIVE_IMAGE),
            eq("1"),
            eq(100L)
        );
        assertThat(fileIdsCaptor.getValue()).containsExactly("img1", "img2");

        then(workspaceRequestImageRepository).should().saveAll(imagesCaptor.capture());
        assertThat(imagesCaptor.getValue()).extracting(WorkspaceRequestImage::getFileId)
            .containsExactly("img1", "img2");
        assertThat(imagesCaptor.getValue()).extracting(WorkspaceRequestImage::getSortOrder)
            .containsExactly(0, 1);
    }

    @Test
    @DisplayName("대표이미지가 5개를 초과하면 ILLEGAL_ARGUMENT 예외가 발생한다")
    void execute_대표이미지5개초과_예외() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(100L);
        BusinessType businessType = businessType(false);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
        given(workspaceRequestRepository.save(any())).willReturn(1L);

        CreateWorkspaceRequestDto dto = baseRequest();
        Set<WorkspaceImageRequestDto> images = new LinkedHashSet<>();
        for (int i = 1; i <= 6; i++) {
            images.add(new WorkspaceImageRequestDto("img" + i, i));
        }
        dto.setRepresentativeImages(images);

        // when & then
        assertThatThrownBy(() -> createWorkspaceRequest.execute(user, dto))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        then(workspaceRequestImageRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("존재하지 않는 업종 ID 면 ILLEGAL_ARGUMENT 예외가 발생하고 신청을 저장하지 않는다")
    void execute_존재하지않는업종_예외() {
        // given
        User user = mock(User.class);
        CreateWorkspaceRequestDto dto = baseRequest();
        dto.setBusinessTypeId(99L);
        given(businessTypeRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> createWorkspaceRequest.execute(user, dto))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        then(workspaceRequestRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("'기타' 업종인데 상세 입력이 없으면 ILLEGAL_ARGUMENT 예외가 발생한다")
    void execute_기타_상세없음_예외() {
        // given
        User user = mock(User.class);
        CreateWorkspaceRequestDto dto = baseRequest();
        dto.setBusinessTypeDetail("   ");
        BusinessType businessType = businessType(true);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));

        // when & then
        assertThatThrownBy(() -> createWorkspaceRequest.execute(user, dto))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
        then(workspaceRequestRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("'기타' 업종이면 상세 입력을 trim 하여 신청에 저장한다")
    void execute_기타_상세있음_저장() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(100L);
        BusinessType etc = businessType(true);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(etc));
        given(workspaceRequestRepository.save(any())).willReturn(1L);

        CreateWorkspaceRequestDto dto = baseRequest();
        dto.setBusinessTypeDetail("  떡볶이 전문점  ");

        // when
        createWorkspaceRequest.execute(user, dto);

        // then
        then(workspaceRequestRepository).should().save(workspaceRequestCaptor.capture());
        assertThat(workspaceRequestCaptor.getValue().getBusinessType()).isEqualTo(etc);
        assertThat(workspaceRequestCaptor.getValue().getBusinessTypeDetail()).isEqualTo("떡볶이 전문점");
    }

    @Test
    @DisplayName("'기타'가 아닌 업종이면 상세 입력이 들어와도 무시(null)한다")
    void execute_비기타_상세무시() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(100L);
        BusinessType businessType = businessType(false);
        given(businessTypeRepository.findById(1L)).willReturn(Optional.of(businessType));
        given(workspaceRequestRepository.save(any())).willReturn(1L);

        CreateWorkspaceRequestDto dto = baseRequest();
        dto.setBusinessTypeDetail("무시되어야 함");

        // when
        createWorkspaceRequest.execute(user, dto);

        // then
        then(workspaceRequestRepository).should().save(workspaceRequestCaptor.capture());
        assertThat(workspaceRequestCaptor.getValue().getBusinessTypeDetail()).isNull();
    }
}
