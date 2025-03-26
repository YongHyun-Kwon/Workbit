package com.workbit.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbit.auth.config.TestEmailConfig;
import com.workbit.authservice.AuthServiceApplication;
import com.workbit.authservice.dto.EmailVerificationRequest;
import com.workbit.authservice.dto.EmailVerificationConfirm;
import com.workbit.authservice.dto.PasswordResetRequest;
import com.workbit.authservice.dto.PasswordResetConfirm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AuthServiceApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestEmailConfig.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("이메일 인증 메일 전송")
    void sendEmailVerification() throws Exception {
        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setEmail("verify@workbit.io");

        mockMvc.perform(post("/api/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @Order(2)
    @DisplayName("잘못된 토큰 인증 실패")
    void confirmEmailTokenFail() throws Exception {
        EmailVerificationConfirm request = new EmailVerificationConfirm();
        request.setToken("invalid-or-expired-token");

        mockMvc.perform(post("/api/email/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // 비즈니스 에러지만 HTTP 200
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("비밀번호 재설정 메일 전송")
    void sendPasswordResetEmail() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("reset@workbit.io");

        mockMvc.perform(post("/api/email/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @Order(4)
    @DisplayName("비밀번호 재설정 실패 - 만료된 토큰")
    void passwordResetFail() throws Exception {
        PasswordResetConfirm request = new PasswordResetConfirm();
        request.setToken("expired-token");
        request.setNewPassword("newpass123");

        mockMvc.perform(post("/api/email/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // 내부 로직 실패지만 HTTP 200
                .andExpect(jsonPath("$.data").value(false));
    }
}