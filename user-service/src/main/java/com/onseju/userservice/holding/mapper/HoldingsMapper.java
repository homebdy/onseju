package com.onseju.userservice.holding.mapper;

import org.springframework.stereotype.Component;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.events.MatchedEvent;
import com.onseju.userservice.order.BeforeTradeOrderDto;
import com.onseju.userservice.events.UpdateEvent;
import com.onseju.userservice.holding.service.dto.AfterTradeHoldingsDto;
import com.onseju.userservice.holding.service.dto.BeforeTradeHoldingsDto;

@Component
public class HoldingsMapper {

	public AfterTradeHoldingsDto toAfterTradeHoldingsDto(
			final MatchedEvent event,
			final Long accountId,
			final Type type
	) {
		return AfterTradeHoldingsDto.builder()
				.type(type)
				.accountId(accountId)
				.companyCode(event.companyCode())
				.quantity(event.quantity())
				.price(event.price())
				.build();
	}

	public BeforeTradeHoldingsDto toBeforeTradeHoldingsDto(
			final BeforeTradeOrderDto dto,
			final Type type,
			final Long accountId
	) {
		return BeforeTradeHoldingsDto.builder()
				.type(type)
				.accountId(accountId)
				.companyCode(dto.companyCode())
				.totalQuantity(dto.totalQuantity())
				.build();
	}
}
