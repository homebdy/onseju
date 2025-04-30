package com.onseju.orderservice.order.controller.resposne;

import com.onseju.orderservice.order.domain.Type;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        String companyCode,
        Type type,
        BigDecimal totalQuantity,
        BigDecimal price
) {
}