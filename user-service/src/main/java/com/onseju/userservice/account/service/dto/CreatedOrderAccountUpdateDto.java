package com.onseju.userservice.account.service.dto;

import com.onseju.userservice.account.domain.Type;

import java.math.BigDecimal;

public record CreatedOrderAccountUpdateDto(
        Long memberId,
        Type type,
        BigDecimal price,
        BigDecimal totalQuantity
) {
}
