package com.onseju.matchingservice.service;

import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.Type;
import com.onseju.matchingservice.engine.MatchingEngine;
import com.onseju.matchingservice.events.OrderCreatedEvent;
import com.onseju.matchingservice.events.listener.MatchingEventListener;
import com.onseju.matchingservice.mapper.EventMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class OrderedEventHandlerTest {

	@Autowired
    MatchingEventListener matchingEventListener;

	@Autowired
	EventMapper eventMapper;

	@Autowired
	MatchingEngine matchingEngine;

	@Test
	@DisplayName("이벤트를 전달받아 비동기로 처리한다.")
	void handleOrderEventShouldProcessOrder() {
		// given
		OrderCreatedEvent orderedEvent = new OrderCreatedEvent(
				UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
				1L,
				"005930",
				Type.LIMIT_BUY,
				OrderStatus.ACTIVE,
				new BigDecimal(100),
				new BigDecimal(100),
				new BigDecimal(100),
				Instant.now().toEpochMilli(),
				1L
		);

		// when
		CompletableFuture.runAsync(() -> matchingEventListener.handleOrderEvent(orderedEvent))
				.orTimeout(2, TimeUnit.SECONDS) // 비동기 실행을 기다림
				.join();

		// then
		Assertions.assertThatCode(() -> matchingEngine.processOrder(eventMapper.toTradeOrder(orderedEvent)))
				.doesNotThrowAnyException();
	}
}
