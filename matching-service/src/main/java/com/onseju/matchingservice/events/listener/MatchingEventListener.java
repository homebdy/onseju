package com.onseju.matchingservice.events.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.onseju.matchingservice.config.RabbitMQConfig;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.MatchingEngine;
import com.onseju.matchingservice.events.OrderCreatedEvent;
import com.onseju.matchingservice.mapper.EventMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MatchingEventListener {

	private final MatchingEngine matchingEngine;
	private final EventMapper eventMapper;

	/**
	 * 주문 검증 이벤트 처리
	 * 유저 서비스에서 주문 검증이 완료되면 실제 매칭 프로세스 시작
	 */
	@RabbitListener(queues = RabbitMQConfig.MATCHING_REQUEST_QUEUE)
	public void handleOrderEvent(OrderCreatedEvent event) {
		// 주문 생성
		final TradeOrder order = eventMapper.toTradeOrder(event);

		// 검증 완료된 주문을 매칭 엔진에 전달하여 매칭 시작
		matchingEngine.processOrder(order);
	}
}
