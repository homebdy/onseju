package com.onseju.userservice.account.mapper;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.service.dto.CreatedOrderAccountUpdateDto;
import com.onseju.userservice.order.CreatedOrderDto;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

	public CreatedOrderAccountUpdateDto toCreatedOrderAccountUpdateDto(
			final CreatedOrderDto dto,
			final Type type,
			Long memberId
	) {
		return new CreatedOrderAccountUpdateDto(
				memberId,
				type,
				dto.price(),
				dto.totalQuantity()
		);
	}
}
