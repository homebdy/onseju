package com.onseju.orderservice.events.exception;

import com.onseju.orderservice.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class MatchedEventPublisherFailException extends BaseException {

    public MatchedEventPublisherFailException() {
        super("ORDER SERVICE - 체결 완료 이벤트 발행 실패", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
