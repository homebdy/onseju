package com.onseju.userservice.holding.exception;

import com.onseju.userservice.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class HoldingsNotFoundException extends BaseException {
    public HoldingsNotFoundException() {
        super("보유하지 않은 종목 입니다.", HttpStatus.NOT_FOUND);
    }
}
