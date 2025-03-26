package com.workbit.authservice.service;

import com.workbit.authservice.domain.RefreshToken;
import com.workbit.authservice.domain.User;
import com.workbit.authservice.dto.AuthResponse;
import com.workbit.authservice.dto.TokenRefreshRequest;
import com.workbit.authservice.dto.UserSummary;
import com.workbit.authservice.repository.RefreshTokenRepository;
import com.workbit.authservice.repository.UserRepository;
import com.workbit.common.service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public String generateRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken().builder()
                .userId(userId)
                .token(token)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public AuthResponse refresh(TokenRefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .filter(t -> !t.isExpired() && !t.isRevoked())
                .orElseThrow(() -> new RuntimeException("refresh token not found"));

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("user not found"));

        token.revoke();
        refreshTokenRepository.save(token);

        String newAccessToken = jwtTokenProvider.createToken(user.getEmail());
        String newRefreshToken = generateRefreshToken(user.getId());

        return new AuthResponse(newAccessToken,
                new UserSummary(user.getEmail(), user.getName(), user.getNickname()),
                newRefreshToken);
    }

}
