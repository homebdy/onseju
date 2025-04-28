package com.onseju.userservice.account.mapper;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.service.dto.AfterTradeAccountDto;
import com.onseju.userservice.account.service.dto.BeforeTradeAccountDto;
import com.onseju.userservice.events.MatchedEvent;
import com.onseju.userservice.order.BeforeTradeOrderDto;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

	public AfterTradeAccountDto toAfterTradeAccountDto(
			final MatchedEvent event,
			final Long memberId,
			final Type type
	) {
		return AfterTradeAccountDto.builder()
				.memberId(memberId)
				.type(type)
				.price(event.price())
				.quantity(event.quantity())
				.build();
	}

	public BeforeTradeAccountDto toBeforeTradeAccountDto(final BeforeTradeOrderDto dto, final Type type, Long memberId) {
		return BeforeTradeAccountDto.builder()
				.memberId(memberId)
				.type(type)
				.price(dto.price())
				.totalQuantity(dto.totalQuantity())
				.build();
	}
}
