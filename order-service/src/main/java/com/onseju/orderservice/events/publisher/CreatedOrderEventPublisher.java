package com.onseju.orderservice.events.publisher;

import com.onseju.orderservice.events.dto.CreatedOrderEvent;
import com.onseju.orderservice.events.exception.OrderEventPublisherFailException;
import com.onseju.orderservice.global.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CreatedOrderEventPublisher extends AbstractEventPublisher<CreatedOrderEvent> {

    public CreatedOrderEventPublisher(RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate);
    }

    @Override
    protected void validateEvent(CreatedOrderEvent event) {
        if (event == null || event.id() == null) {
            throw new IllegalArgumentException("Invalid order event");
        }
    }

    @Override
    protected void doPublish(CreatedOrderEvent event) {
        try {
            publishOrderCreatedEventToOrderService(event);
            publishOrderCreatedEventToMatchingEngine(event);
        } catch (Exception ex) {
            log.error("주문 이벤트 발행 중 오류 발생. event id: {}", event.id(), ex);
            throw new OrderEventPublisherFailException();
        }
    }

    private void publishOrderCreatedEventToOrderService(CreatedOrderEvent event) {
        sendMessage(
                RabbitMQConfig.ONSEJU_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_KEY,
                event,
                "order-" + event.id()
        );
    }

    private void publishOrderCreatedEventToMatchingEngine(CreatedOrderEvent event) {
        sendMessage(
                RabbitMQConfig.ONSEJU_MATCHING_EXCHANGE,
                RabbitMQConfig.MATCHING_REQUEST_KEY,
                event,
                "matching-" + event.id()
        );
    }
}