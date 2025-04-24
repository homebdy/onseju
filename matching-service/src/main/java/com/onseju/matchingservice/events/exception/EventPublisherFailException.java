package com.onseju.matchingservice.events.exception;

import com.onseju.matchingservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EventPublisherFailException extends BaseException {
    public EventPublisherFailException() {
        super("", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}