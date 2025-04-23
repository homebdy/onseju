package com.onseju.matchingservice.events.exception;

import org.springframework.http.HttpStatus;

import com.onseju.matchingservice.exception.BaseException;

import lombok.Getter;

public class EventPublisherFailException extends BaseException {
	public EventPublisherFailException() {
		super("", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}