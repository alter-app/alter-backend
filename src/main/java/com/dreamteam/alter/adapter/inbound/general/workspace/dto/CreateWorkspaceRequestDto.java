package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@Schema(description = "대표자 이름", example = "홍길동")
	private String ownName;

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
}
