package com.workbit.auth.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class TestEmailConfig {

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSender mock = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mock.createMimeMessage()).thenReturn(mimeMessage);
        return mock;
    }

}
