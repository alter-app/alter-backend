package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.dreamteam.alter.adapter.inbound.common.dto.WorkspaceImageRequestDto;
import com.dreamteam.alter.common.util.PhoneNumberUtil;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.command.CreateWorkspaceRequestCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 등록 신청 DTO")
public class CreateWorkspaceRequestDto {

	@NotBlank
	@Schema(description = "업장 이름", example = "세븐일레븐")
	private String bizName;

	@NotBlank
	@Schema(description = "사업자 등록번호", example = "123-45-12345")
	private String brn;

	@NotBlank
	@Schema(description = "풀주소", example = "서울특별시 구로구 고척동 123")
	private String address;

	@NotBlank
	@Schema(description = "시/도", example = "서울특별시")
	private String province;

	@NotBlank
	@Schema(description = "구", example = "구로구")
	private String district;

	@NotBlank
	@Schema(description = "동", example = "고척동")
	private String town;

	@NotNull
	@Schema(description = "위도", example = "35.150485")
	private BigDecimal latitude;

	@NotNull
	@Schema(description = "경도", example = "129.115717")
	private BigDecimal longitude;

	@NotNull
	@Schema(description = "업종(BusinessType) ID", example = "1")
	private Long businessTypeId;

	@Size(max = 128)
	@Schema(description = "업종 상세 (업종이 '기타'인 경우 필수)", example = "떡볶이 전문점")
	private String businessTypeDetail;

	@NotBlank
	@Size(max = 13)
	@Pattern(regexp = PhoneNumberUtil.LOCAL_PHONE_NUMBER_PATTERN,
		message = "연락처는 0으로 시작하는 9~11자리 숫자 또는 2~3자리-3~4자리-4자리 형식이어야 합니다.")
	@Schema(description = "업장 연락처. 0으로 시작하는 9~11자리 ASCII 숫자 또는 2~3자리-3~4자리-4자리 형식. 접두번호 제한 없이 원문 검증 후 하이픈을 제거하여 숫자로 저장합니다.", example = "02-1234-5678")
	private String contact;

	@NotBlank
	@Schema(description = "사업자등록증명원 파일 ID", example = "01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e")
	private String workspaceCertFileId;

	@NotBlank
	@Schema(description = "대표자 신분증 사본 파일 ID", example = "01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e")
	private String workspaceOwnIdentityFileId;

	@Schema(description = "위임 확인서 파일 ID", example = "01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e")
	private String workspaceWarrantFileId;

	@Size(max = 5, message = "대표이미지는 최대 5개까지 등록할 수 있습니다.")
	@Schema(description = "업장 대표이미지 목록 (최대 5개, fileId 중복 자동 제거, sortOrder 오름차순으로 노출)")
	private Set<@Valid WorkspaceImageRequestDto> representativeImages;

	public List<String> getOrderedRepresentativeImageFileIds() {
		return WorkspaceImageRequestDto.toOrderedFileIds(representativeImages);
	}

	public CreateWorkspaceRequestCommand toCommand(User user) {
		return new CreateWorkspaceRequestCommand(user, bizName, brn, address, province, district, town,
			latitude, longitude, businessTypeId, businessTypeDetail, contact, workspaceCertFileId,
			workspaceOwnIdentityFileId, workspaceWarrantFileId, getOrderedRepresentativeImageFileIds());
	}
}
