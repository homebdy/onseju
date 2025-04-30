package com.onseju.userservice.events.dto;

import com.onseju.userservice.account.domain.Type;

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
