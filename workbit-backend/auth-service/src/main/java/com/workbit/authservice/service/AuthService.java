package com.workbit.authservice.service;

import com.workbit.authservice.domain.User;
import com.workbit.authservice.dto.AuthResponse;
import com.workbit.authservice.dto.SignInRequest;
import com.workbit.authservice.dto.SignUpRequest;
import com.workbit.authservice.dto.UserSummary;
import com.workbit.authservice.repository.UserRepository;
import com.workbit.common.service.exception.UserNotFoundException;
import com.workbit.common.service.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    public void signup(SignUpRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .profileImageUrl(request.getProfileImageUrl())
                .status(request.getStatus())
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(SignInRequest request) throws UserNotFoundException {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = tokenService.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken,
                new UserSummary(user.getEmail(), user.getName(), user.getNickname()),
                refreshToken);
    }

    public boolean checkEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}
