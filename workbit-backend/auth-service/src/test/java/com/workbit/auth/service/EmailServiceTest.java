package com.workbit.auth.service;

import com.workbit.authservice.domain.EmailVerificationToken;
import com.workbit.authservice.repository.EmailVerificationTokenRepository;
import com.workbit.authservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendVerificationTokenSuccess() {
        String email = "test@workbit.io";

        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        emailService.sendVerificationToken(email);

        verify(tokenRepository, times(1)).save(any());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void verifyTokenSuccess() {
        String token = "valid-token";
        EmailVerificationToken mockToken = EmailVerificationToken.builder()
                .token(token)
                .used(false)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        boolean result = emailService.verifyToken(token);

        assertTrue(result);
        assertTrue(mockToken.isUsed());
    }

    @Test
    void verifyTokenFail() {
        String token = "expired";
        EmailVerificationToken mockToken = EmailVerificationToken.builder()
                .token(token)
                .used(false)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(mockToken));

        boolean result = emailService.verifyToken(token);
        assertFalse(result);
    }
}
