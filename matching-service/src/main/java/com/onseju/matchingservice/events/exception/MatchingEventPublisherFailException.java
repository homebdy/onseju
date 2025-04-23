package com.onseju.matchingservice.events.exception;

import org.springframework.http.HttpStatus;

import com.onseju.matchingservice.exception.BaseException;

public class MatchingEventPublisherFailException extends BaseException {
	public MatchingEventPublisherFailException() {
		super("", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
