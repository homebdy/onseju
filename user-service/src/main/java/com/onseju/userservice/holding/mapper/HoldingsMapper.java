package com.onseju.userservice.holding.mapper;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.holding.service.dto.CreatedOrderHoldingsUpdateDto;
import com.onseju.userservice.order.CreatedOrderDto;
import org.springframework.stereotype.Component;

@Component
public class HoldingsMapper {

	public CreatedOrderHoldingsUpdateDto toOrderCreatedHoldingsUpdateDto(
			final CreatedOrderDto dto,
			final Type type,
			final Long memberId
	) {
		return new CreatedOrderHoldingsUpdateDto(
				type,
				memberId,
				dto.companyCode(),
				dto.totalQuantity()
		);
	}
}
