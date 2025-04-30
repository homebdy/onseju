package com.onseju.userservice.account.exception;

import com.onseju.userservice.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BaseException {
    public InsufficientBalanceException() {
        super("주문금액이 예수금잔액을 초과합니다.", HttpStatus.BAD_REQUEST);
    }
}

