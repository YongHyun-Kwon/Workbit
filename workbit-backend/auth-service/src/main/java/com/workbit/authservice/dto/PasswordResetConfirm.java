package com.workbit.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirm {

    @NotBlank(message = "토큰이 필요합니다.")
    private String token;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    private String newPassword;

}