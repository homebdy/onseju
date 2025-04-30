package com.onseju.orderservice.order.mapper;

import com.onseju.orderservice.events.dto.CreatedOrderEvent;
import com.onseju.orderservice.events.dto.MatchedOrderUpdateEvent;
import com.onseju.orderservice.order.controller.resposne.OrderResponse;
import com.onseju.orderservice.order.domain.Order;
import com.onseju.orderservice.order.domain.OrderStatus;
import com.onseju.orderservice.order.dto.MatchedOrderUpdateDto;
import com.onseju.orderservice.order.service.dto.OrderCreateCommand;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderMapper {

    public Order toEntity(final Long orderId, final OrderCreateCommand dto, final Long memberId) {
        return Order.builder()
                .id(orderId)
                .companyCode(dto.companyCode())
                .type(dto.type())
                .totalQuantity(dto.totalQuantity())
                .remainingQuantity(dto.totalQuantity())
                .status(OrderStatus.ACTIVE)
                .price(dto.price())
                .memberId(memberId)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    public Order toEntity(final CreatedOrderEvent event) {
        return Order.builder()
                .id(event.orderId())
                .companyCode(event.companyCode())
                .type(event.type())
                .totalQuantity(event.totalQuantity())
                .remainingQuantity(event.totalQuantity())
                .status(OrderStatus.ACTIVE)
                .price(event.price())
                .memberId(event.memberId())
                .timestamp(event.timestamp())
                .build();
    }

    public CreatedOrderEvent toCreatedOrderEvent(final Order order) {
        return CreatedOrderEvent.builder()
                .id(UUID.randomUUID())
                .orderId(order.getId())
                .companyCode(order.getCompanyCode())
                .type(order.getType())
                .status(order.getStatus())
                .totalQuantity(order.getTotalQuantity())
                .remainingQuantity(order.getRemainingQuantity())
                .price(order.getPrice())
                .timestamp(order.getTimestamp())
                .memberId(order.getMemberId())
                .build();
    }

    public MatchedOrderUpdateEvent toMatchedOrderUpdateEvent(final Order order, final MatchedOrderUpdateDto dto) {
        return new MatchedOrderUpdateEvent(
                UUID.randomUUID(),
                order.getType(),
                order.getCompanyCode(),
                order.getMemberId(),
                dto.quantity(),
                dto.price(),
                dto.tradeAt()
        );
    }

    public OrderResponse toOrderResponse(final Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCompanyCode(),
                order.getType(),
                order.getTotalQuantity(),
                order.getPrice()
        );
    }
}
