package com.onseju.matchingservice.handler;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.MatchingEngine;
import com.onseju.matchingservice.events.OrderCreatedEvent;
import com.onseju.matchingservice.mapper.EventMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderEventHandler {

	private final MatchingEngine matchingEngine;
	private final EventMapper eventMapper;

	/**
	 * 주문 생성 이벤트를 받아 체결 엔진으로 넘긴다.
	 */
	@Async
	@EventListener
	public void handleOrderEvent(OrderCreatedEvent orderedEvent) {
		TradeOrder order = eventMapper.toTradeOrder(orderedEvent);
		matchingEngine.processOrder(order);
	}
}
