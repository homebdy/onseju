package com.onseju.orderservice.events.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.onseju.orderservice.order.domain.OrderStatus;
import com.onseju.orderservice.order.domain.Type;

import lombok.Builder;

@Builder
public record CreatedOrderEvent(
		UUID id,
		Long orderId,
		String companyCode,
		Type type,
		OrderStatus status,
		BigDecimal totalQuantity,
		BigDecimal remainingQuantity,
		BigDecimal price,
		Long timestamp,
		Long accountId
) {
}
