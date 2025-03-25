package com.workbit.auth.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerTest {

    @Test
    void sampleTest() {
        String token = "1234";
        assertThat(token).isEqualTo("1234");
    }
}
