package com.onseju.userservice.holding.exception;

import com.onseju.userservice.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientHoldingsException extends BaseException {
    public InsufficientHoldingsException() {
        super("충분한 수량을 보유하지 않은 종목 입니다.", HttpStatus.BAD_REQUEST);
    }
}
