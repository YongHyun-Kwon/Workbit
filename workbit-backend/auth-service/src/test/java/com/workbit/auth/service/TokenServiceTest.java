package com.workbit.auth.service;

import com.workbit.authservice.domain.RefreshToken;
import com.workbit.authservice.domain.User;
import com.workbit.authservice.dto.AuthResponse;
import com.workbit.authservice.dto.TokenRefreshRequest;
import com.workbit.authservice.repository.RefreshTokenRepository;
import com.workbit.authservice.repository.UserRepository;
import com.workbit.authservice.service.TokenService;
import com.workbit.common.service.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void generateRefreshTokenSuccess() {
        Long userId = 1L;

        String token = tokenService.generateRefreshToken(userId);

        assertNotNull(token);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void refreshTokenSuccess() {
        String oldToken = "valid-refresh";
        String newAccess = "new-access";

        RefreshToken existing = RefreshToken.builder()
                .token(oldToken)
                .userId(1L)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        User user = User.builder().id(1L).email("token@workbit.io").name("토큰맨").nickname("jwtman").build();

        when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createToken(user.getEmail())).thenReturn(newAccess);

        AuthResponse response = tokenService.refresh(new TokenRefreshRequest(oldToken));
        assertEquals(newAccess, response.getToken());
        assertEquals("token@workbit.io", response.getUser().getEmail());
    }

    @Test
    void refreshTokenFailure() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired")
                .revoked(false)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThrows(RuntimeException.class,
                () -> tokenService.refresh(new TokenRefreshRequest("expired")));
    }
}
