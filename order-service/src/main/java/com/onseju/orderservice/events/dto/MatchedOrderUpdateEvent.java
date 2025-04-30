package com.onseju.orderservice.events.dto;

import com.onseju.orderservice.order.domain.Type;

import java.math.BigDecimal;
import java.util.UUID;

public record MatchedOrderUpdateEvent(
        UUID id,
        Type type,
        String companyCode,
        Long memberId,
        BigDecimal quantity,
        BigDecimal price,
        Long tradeAt
) {
}
