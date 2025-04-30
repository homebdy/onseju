package com.onseju.userservice.holding.service.dto;

import com.onseju.userservice.account.domain.Type;

import java.math.BigDecimal;

public record CreatedOrderHoldingsUpdateDto(
		Type type,
		Long memberId,
		String companyCode,
		BigDecimal totalQuantity
) {
}
