package com.onseju.userservice.order;


import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record BeforeTradeOrderDto(
		String companyCode,
		String type,
		BigDecimal totalQuantity,
		BigDecimal price,
		Long timestamp,
		Long memberId
) {

}
