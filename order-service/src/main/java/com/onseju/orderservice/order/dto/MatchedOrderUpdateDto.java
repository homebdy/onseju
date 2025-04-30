package com.onseju.orderservice.order.dto;

import java.math.BigDecimal;

public record MatchedOrderUpdateDto(
        Long orderId,
        BigDecimal quantity,
        BigDecimal price,
        Long tradeAt
) {
}
