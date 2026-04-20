package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 변경 요청 DTO")
public class UpdatePasswordRequestDto {

    @Schema(description = "현재 비밀번호 (비밀번호가 설정된 사용자는 필수, 소셜 전용 사용자는 생략 가능)", example = "currentPass1!")
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 16)
    @Schema(description = "새 비밀번호 (8~16자, 영문·숫자·특수문자 각 1개 이상)", example = "newPass1!")
    private String newPassword;

    @AssertTrue(message = "새 비밀번호는 현재 비밀번호와 달라야 합니다")
    private boolean isNewPasswordDifferent() {
        if (currentPassword == null || newPassword == null) return true;
        return !currentPassword.equals(newPassword);
    }
}
