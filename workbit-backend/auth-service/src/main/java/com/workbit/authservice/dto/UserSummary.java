package com.workbit.authservice.dto;

import com.workbit.authservice.domain.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummary {
    private String email;
    private String name;
    private String nickname;
}
