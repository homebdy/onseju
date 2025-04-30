package com.onseju.orderservice.events.mapper;

import com.onseju.orderservice.events.dto.MatchedEvent;
import com.onseju.orderservice.order.dto.MatchedOrderUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public MatchedOrderUpdateDto toMatchedOrderUpdateDto(Long orderId, MatchedEvent event) {
        return new MatchedOrderUpdateDto(
                orderId,
                event.quantity(),
                event.price(),
                event.tradeAt()
        );
    }
}
