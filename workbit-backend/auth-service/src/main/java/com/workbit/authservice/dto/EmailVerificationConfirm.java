package com.workbit.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationConfirm {
    private String token;
}