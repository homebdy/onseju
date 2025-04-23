package com.onseju.userservice.account.service.dto;

import java.math.BigDecimal;

import com.onseju.userservice.account.domain.Type;

import lombok.Builder;

@Builder
public record AfterTradeAccountDto(
		Long accountId,
		Type type,
		BigDecimal price,
		BigDecimal quantity
) {
}
