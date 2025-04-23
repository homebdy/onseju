package com.onseju.userservice.account.service.dto;

import com.onseju.userservice.account.domain.Type;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BeforeTradeAccountDto(
		Long memberId,
		Type type,
		BigDecimal price,
		BigDecimal totalQuantity
) {
}
