package com.workbit.authservice.controller;

import com.workbit.authservice.dto.*;
import com.workbit.authservice.secutiry.UserPrincipal;
import com.workbit.authservice.service.AuthService;
import com.workbit.authservice.service.TokenService;
import com.workbit.common.service.exception.UserNotFoundException;
import com.workbit.common.service.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/signup")
    public ApiResponse<String> signup(@Valid @RequestBody SignUpRequest request) {
        authService.signup(request);
        return new ApiResponse<>("success", "회원가입 완료", null);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody SignInRequest request) throws UserNotFoundException {
        return new ApiResponse<>("success", "로그인 성공", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserSummary> getCurrentUser(@AuthenticationPrincipal UserPrincipal user) {
        return new ApiResponse<>("success", "내 정보 조회",
                new UserSummary(user.getEmail(), "이름임시", "닉네임임시"));
    }

    @GetMapping("/email-check")
    public ApiResponse<Boolean> checkEmail(@Valid @RequestParam String email) {
        boolean exists = authService.checkEmail(email);
        return new ApiResponse<>("success", "중복 여부 조회", exists);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return new ApiResponse<>("success", "토큰 재발급 완료", tokenService.refresh(request));
    }
}
