package com.onseju.userservice.holding.service.dto;

import com.onseju.userservice.account.domain.Type;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BeforeTradeHoldingsDto(
		Type type,
		Long memberId,
		String companyCode,
		BigDecimal totalQuantity
) {
}
