package com.onseju.orderservice.order.exception;

import com.onseju.orderservice.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ShortageOfRemainingQuantityException extends BaseException {

    public ShortageOfRemainingQuantityException() {
        super("주문 수량이 부족합니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
