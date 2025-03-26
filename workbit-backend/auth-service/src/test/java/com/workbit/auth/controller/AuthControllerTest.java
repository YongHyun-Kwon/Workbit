package com.workbit.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbit.authservice.AuthServiceApplication;
import com.workbit.authservice.dto.SignInRequest;
import com.workbit.authservice.dto.SignUpRequest;
import com.workbit.authservice.dto.TokenRefreshRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AuthServiceApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("회원가입 성공")
    void signupSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test1@workbit.io");
        request.setPassword("12345678");
        request.setName("테스트유저");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @Order(2)
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signupFail_duplicateEmail() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test1@workbit.io"); // 위에서 이미 가입함
        request.setPassword("12345678");
        request.setName("테스트유저");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value("fail"));
    }

    @Test
    @Order(3)
    @DisplayName("로그인 성공")
    void loginAndRefreshTokenTest() throws Exception {
        SignInRequest request = new SignInRequest();
        request.setEmail("test1@workbit.io");
        request.setPassword("12345678");

        String responseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. JSON 파싱해서 refreshToken 꺼내기
        JsonNode root = objectMapper.readTree(responseJson);
        String refreshToken = root.path("data").path("refreshToken").asText();
        assertNotNull(refreshToken);

        // 3. refresh API 호출
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @Order(4)
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void loginFail_userNotFound() throws Exception {
        SignInRequest request = new SignInRequest();
        request.setEmail("notfound@workbit.io");
        request.setPassword("12345678");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value("fail"));
    }

    @Test
    @Order(5)
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void loginFail_wrongPassword() throws Exception {
        SignInRequest request = new SignInRequest();
        request.setEmail("test1@workbit.io");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value("fail"));
    }

    @Test
    @Order(6)
    @DisplayName("이메일 중복 확인 API 테스트")
    void emailCheck() throws Exception {
        mockMvc.perform(get("/api/auth/email-check")
                        .param("email", "test1@workbit.io"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @DisplayName("리프레시 토큰 재발급 실패 - 만료 or 존재하지 않음")
    @Test
    @Order(7)
    void refreshTokenFail() throws Exception {
        String invalidRefreshToken = "invalid-or-expired-token";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", invalidRefreshToken))))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value("fail"));
    }

}
