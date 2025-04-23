package com.onseju.matchingservice.events.exception;

import org.springframework.http.HttpStatus;

import com.onseju.matchingservice.exception.BaseException;

public class OrderBookSyncEventPublisherFailException extends BaseException {
	public OrderBookSyncEventPublisherFailException() {
		super("", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
