package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	@NotBlank
	@Schema(description = "업장 형태", example = "음식점")
	private String type;

	@NotBlank
	@Schema(description = "업장 연락처", example = "02-1234-5678")
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
	@Schema(description = "업장 대표이미지 파일 ID 목록 (최대 5개, 목록 순서가 노출 순서)", example = "[\"01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e\"]")
	private List<String> representativeImageFileIds;
}
