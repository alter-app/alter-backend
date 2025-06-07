package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.type.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 스크랩한 공고 요약 정보 DTO")
public class UserFavoritePostingListPostingSummaryResponseDto {

    @NotNull
    @Schema(description = "공고 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "업장 이름", example = "카페 드림")
    private String businessName;

    @NotBlank
    @Schema(description = "공고 제목", example = "홀서빙 구합니다")
    private String title;

    @NotNull
    @Schema(description = "급여", example = "10000")
    private int payAmount;

    @NotNull
    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    public static UserFavoritePostingListPostingSummaryResponseDto from (Posting entity) {
        return UserFavoritePostingListPostingSummaryResponseDto.builder()
            .id(entity.getId())
            .businessName(entity.getWorkspace().getBusinessName())
            .title(entity.getTitle())
            .payAmount(entity.getPayAmount())
            .paymentType(entity.getPaymentType())
            .build();
    }

}
