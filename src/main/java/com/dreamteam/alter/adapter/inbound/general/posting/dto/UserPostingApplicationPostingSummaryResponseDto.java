package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.adapter.inbound.general.workspace.dto.PostingDetailWorkspaceResponseDto;
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
public class UserPostingApplicationPostingSummaryResponseDto {

    @NotNull
    @Schema(description = "공고 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "업장 정보", example = "1")
    private PostingDetailWorkspaceResponseDto workspace;

    @NotBlank
    @Schema(description = "공고 제목", example = "홀서빙 구합니다")
    private String title;

    @NotBlank
    @Schema(description = "공고 설명", example = "홀서빙 구합니다. 많은 지원 부탁드립니다.")
    private String description;

    @NotNull
    @Schema(description = "급여", example = "10000")
    private int payAmount;

    @NotNull
    @Schema(description = "급여 타입", example = "HOURLY")
    private PaymentType paymentType;

    public static UserPostingApplicationPostingSummaryResponseDto from(Posting entity) {
        return UserPostingApplicationPostingSummaryResponseDto.builder()
            .id(entity.getId())
            .workspace(
                PostingDetailWorkspaceResponseDto.from(entity.getWorkspace())
            )
            .title(entity.getTitle())
            .description(entity.getDescription())
            .payAmount(entity.getPayAmount())
            .paymentType(entity.getPaymentType())
            .build();
    }

}
