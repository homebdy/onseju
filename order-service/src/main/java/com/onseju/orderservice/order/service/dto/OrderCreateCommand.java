package com.onseju.orderservice.order.service.dto;

import com.onseju.orderservice.order.domain.Type;

import java.math.BigDecimal;

public record OrderCreateCommand(
        String companyCode,
        Type type,
        BigDecimal totalQuantity,
        BigDecimal price,
        Long memberId
) {
}
