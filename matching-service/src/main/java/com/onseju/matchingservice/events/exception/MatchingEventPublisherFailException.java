package com.onseju.matchingservice.events.exception;

import com.onseju.matchingservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class MatchingEventPublisherFailException extends BaseException {
    public MatchingEventPublisherFailException() {
        super("", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
