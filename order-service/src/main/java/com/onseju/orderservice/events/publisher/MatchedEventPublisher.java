package com.onseju.orderservice.events.publisher;

import com.onseju.orderservice.events.dto.MatchedOrderUpdateEvent;
import com.onseju.orderservice.events.exception.MatchedEventPublisherFailException;
import com.onseju.orderservice.global.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MatchedEventPublisher extends AbstractEventPublisher<MatchedOrderUpdateEvent> {

    public MatchedEventPublisher(RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate);
    }

    @Override
    protected void validateEvent(MatchedOrderUpdateEvent event) {
        if (event == null || event.id() == null) {
            throw new IllegalArgumentException("Invalid matched order event");
        }
    }

    @Override
    protected void doPublish(MatchedOrderUpdateEvent event) {
        try {
            publishToUserService(event);
        } catch (Exception ex) {
            log.error("체결 완료 이벤트 발행 중 오류 발생. event id: {}", event.id(), ex);
            throw new MatchedEventPublisherFailException();
        }
    }

    private void publishToUserService(MatchedOrderUpdateEvent event) {
        sendMessage(
                RabbitMQConfig.ONSEJU_EXCHANGE,
                RabbitMQConfig.USER_UPDATE_KEY,
                event,
                "user update -" + event.id()
        );
    }
}
