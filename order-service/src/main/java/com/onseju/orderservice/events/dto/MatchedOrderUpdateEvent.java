package com.onseju.orderservice.events.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MatchedOrderUpdateEvent(
        UUID id,
        String companyCode,
        Long memberId,
        BigDecimal quantity,
        BigDecimal price,
        Long tradeAt
) {
}
