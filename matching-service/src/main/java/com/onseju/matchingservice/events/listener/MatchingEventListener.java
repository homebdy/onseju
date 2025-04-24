package com.onseju.matchingservice.events.listener;

import com.onseju.matchingservice.config.RabbitMQConfig;
import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.MatchingEngine;
import com.onseju.matchingservice.events.OrderCreatedEvent;
import com.onseju.matchingservice.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingEventListener {

    private final MatchingEngine matchingEngine;
    private final EventMapper eventMapper;

    /**
     * 주문 생성 이벤트 수신
     */
    @RabbitListener(queues = RabbitMQConfig.MATCHING_REQUEST_QUEUE)
    public void handleOrderEvent(OrderCreatedEvent event) {
        final TradeOrder order = eventMapper.toTradeOrder(event);

        matchingEngine.processOrder(order);
    }
}
