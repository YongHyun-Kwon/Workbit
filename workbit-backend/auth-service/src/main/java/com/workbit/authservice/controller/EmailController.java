package com.workbit.authservice.controller;

import com.workbit.authservice.dto.*;
import com.workbit.authservice.service.EmailService;
import com.workbit.authservice.service.PasswordResetService;
import com.workbit.common.service.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/verify")
    public ApiResponse<String> sendVerification(@Valid @RequestBody EmailVerificationRequest request) {
        emailService.sendVerificationToken(request.getEmail());
        return new ApiResponse<>("success", "인증 메일 전송 완료", null);
    }

    @PostMapping("/confirm")
    public ApiResponse<Boolean> confirmVerification(@RequestBody EmailVerificationConfirm request) {
        boolean verified = emailService.verifyToken(request.getToken());
        return new ApiResponse<>("success", "인증 결과", verified);
    }

    @PostMapping("/password-reset")
    public ApiResponse<String> sendPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.sendResetToken(request.getEmail());
        return new ApiResponse<>("success", "비밀번호 재설정 메일 전송 완료", null);
    }

    @PostMapping("/password-reset/confirm")
    public ApiResponse<Boolean> confirmReset(@Valid @RequestBody PasswordResetConfirm request) {
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return new ApiResponse<>("success", "비밀번호 재설정 결과", success);
    }
}