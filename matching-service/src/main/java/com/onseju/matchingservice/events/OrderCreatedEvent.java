package com.onseju.matchingservice.events;

import com.onseju.matchingservice.domain.OrderStatus;
import com.onseju.matchingservice.domain.Type;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
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
