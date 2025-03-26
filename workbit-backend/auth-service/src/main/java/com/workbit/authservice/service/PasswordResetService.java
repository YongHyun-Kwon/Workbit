package com.workbit.authservice.service;

import com.workbit.authservice.domain.EmailVerificationToken;
import com.workbit.authservice.domain.User;
import com.workbit.authservice.repository.EmailVerificationTokenRepository;
import com.workbit.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void sendResetToken(String email) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken entity = EmailVerificationToken.builder()
                .email(email)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        tokenRepository.save(entity);

        String content = "비밀번호 재설정 링크: http://localhost:8081/api/email/password-reset/confirm?token=" + token;
        emailService.sendMail(email, "[Workbit] 비밀번호 재설정", content);
    }

    public boolean resetPassword(String token, String newPassword) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isUsed() && t.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(t -> {
                    User user = userRepository.findByEmail(t.getEmail()).orElse(null);
                    if (user == null) return false;
                    user.changePassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    t.setUsed(true);
                    tokenRepository.save(t);
                    return true;
                }).orElse(false);
    }
}
