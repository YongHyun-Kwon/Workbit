package com.workbit.authservice.service;

import com.workbit.authservice.domain.EmailVerificationToken;
import com.workbit.authservice.repository.EmailVerificationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    public void sendVerificationToken(String email) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken entity = EmailVerificationToken.builder()
                .email(email)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        tokenRepository.save(entity);

        String subject = "[Workbit] 이메일 인증 요청";
        String content = "다음 링크를 클릭해 인증을 완료하세요: " +
                "http://localhost:8081/api/email/confirm?token=" + token;

        sendMail(email, subject, content);
    }

    public boolean verifyToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isUsed() && t.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(t -> {
                    t.setUsed(true);
                    tokenRepository.save(t);
                    return true;
                }).orElse(false);
    }

    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }
}
