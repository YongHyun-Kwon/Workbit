package com.workbit.authservice.controller;

import com.workbit.common.service.exception.UserNotFoundException;
import com.workbit.common.service.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/error-sample")
    public ApiResponse<String> errorSample() throws UserNotFoundException {
        throw new UserNotFoundException("존재하지 않는 사용자입니다.");
    }
}
