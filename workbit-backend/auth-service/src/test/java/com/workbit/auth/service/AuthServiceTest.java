package com.workbit.auth.service;

import com.workbit.authservice.domain.User;
import com.workbit.authservice.dto.SignInRequest;
import com.workbit.authservice.dto.SignUpRequest;
import com.workbit.authservice.repository.UserRepository;
import com.workbit.authservice.service.AuthService;
import com.workbit.authservice.service.TokenService;
import com.workbit.common.service.exception.UserNotFoundException;
import com.workbit.common.service.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signUpSuccess() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@test.com");
        request.setPassword("123456");
        request.setName("test");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    }

    @Test
    void signUpFail() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@example.com");
        request.setPassword("12345678");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));
    }

    @Test
    void loginSuccess() throws UserNotFoundException {
        String email = "test@example.com";
        String rawPassword = "12345678";
        String encodedPassword = "encodedPwd";

        SignInRequest request = new SignInRequest();
        request.setEmail(email);
        request.setPassword(rawPassword);

        User mockUser = User.builder().email(email).password(encodedPassword).name("테스터").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(tokenService.generateRefreshToken(anyLong())).thenReturn("mockRefreshToken");
        when(jwtTokenProvider.createToken(email)).thenReturn("mockAccessToken");

        var response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mockAccessToken");
        assertThat(response.getUser().getEmail()).isEqualTo(email);
    }

    @Test
    void loginFail() {
        SignInRequest request = new SignInRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("123456");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void checkEmail() {
        String email = "exist@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        boolean result = authService.checkEmail(email);

        assertTrue(result);
    }


}
