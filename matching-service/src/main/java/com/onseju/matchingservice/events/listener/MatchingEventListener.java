package com.onseju.matchingservice.events.listener;

import com.onseju.matchingservice.config.RabbitMQConfig;
import com.onseju.matchingservice.events.OrderCreatedEvent;
import com.onseju.matchingservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingEventListener {

    private final MatchingService matchingService;

    /**
     * 주문 생성 이벤트 수신
     */
    @RabbitListener(queues = RabbitMQConfig.MATCHING_REQUEST_QUEUE)
    public void handleOrderEvent(OrderCreatedEvent event) {
        matchingService.matchOrder(event);
    }
}
