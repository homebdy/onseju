package com.onseju.userservice.member.controller.response;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoginResponseDto {
    private final Long userId;
    private final String username;
    private final BigDecimal balance;

    public LoginResponseDto(Long userId, String username, BigDecimal balance) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
    }
}