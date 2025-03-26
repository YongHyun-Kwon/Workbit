package com.workbit.auth.service;

import com.workbit.authservice.domain.EmailVerificationToken;
import com.workbit.authservice.domain.User;
import com.workbit.authservice.repository.EmailVerificationTokenRepository;
import com.workbit.authservice.repository.UserRepository;
import com.workbit.authservice.service.EmailService;
import com.workbit.authservice.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void sendResetTokenSendMailSuccess() {
        String email = "reset@workbit.io";

        passwordResetService.sendResetToken(email);

        verify(tokenRepository, times(1)).save(any());
        verify(emailService, times(1)).sendMail(eq(email), any(), any());
    }

    @Test
    void resetPasswordSuccess() {
        String token = "reset-token";
        String newPassword = "new1234";

        User user = User.builder().email("reset@workbit.io").build();
        EmailVerificationToken mockToken = EmailVerificationToken.builder()
                .token(token)
                .email(user.getEmail())
                .used(false)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded");

        boolean result = passwordResetService.resetPassword(token, newPassword);

        assertTrue(result);
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordFail() {
        String token = "expired";
        EmailVerificationToken mockToken = EmailVerificationToken.builder()
                .token(token)
                .used(false)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        boolean result = passwordResetService.resetPassword(token, "pw");
        assertFalse(result);
    }
}
