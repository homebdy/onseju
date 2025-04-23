package com.onseju.userservice.member.exception;

import com.onseju.userservice.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class NotAuthorizedException extends BaseException {
    public NotAuthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
