package com.onseju.orderservice.order.mapper;

import com.onseju.orderservice.events.dto.CreatedOrderEvent;
import com.onseju.orderservice.order.domain.Order;
import com.onseju.orderservice.order.domain.OrderStatus;
import com.onseju.orderservice.order.domain.Type;
import com.onseju.orderservice.order.service.dto.OrderCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

	private final OrderMapper orderMapper = new OrderMapper();

	@Test
	@DisplayName("OrderRequest를 Entity로 변환한다.")
	void toEntity() {
		// given
		String companyCode = "005930";
		OrderCreateCommand dto = new OrderCreateCommand(
				"005930",
				Type.LIMIT_BUY,
				new BigDecimal(100),
				new BigDecimal(1000), 1L);

		// when
		Order order = orderMapper.toEntity(1L, dto, 1L);

		// then
		assertThat(order).isNotNull();
		assertThat(order.getCompanyCode()).isEqualTo(companyCode);
	}

	@Test
	@DisplayName("OrderCreatedEvent를 Entity로 변환한다.")
	void toEntityFromEvent() {
		// given
		CreatedOrderEvent createdOrderEvent = CreatedOrderEvent.builder()
			.id(UUID.randomUUID())
			.orderId(1L)
			.companyCode("005930")
			.type(Type.LIMIT_BUY)
			.status(OrderStatus.ACTIVE)
			.totalQuantity(new BigDecimal(100))
			.remainingQuantity(new BigDecimal(100))
			.price(new BigDecimal(50000))
			.timestamp(Instant.now().toEpochMilli())
			.accountId(1L)
			.build();

		// when
		Order order = orderMapper.toEntity(createdOrderEvent);

		// then
		assertThat(order).isNotNull();
		assertThat(order.getId()).isEqualTo(createdOrderEvent.orderId());
		assertThat(order.getCompanyCode()).isEqualTo(createdOrderEvent.companyCode());
		assertThat(order.getType()).isEqualTo(createdOrderEvent.type());
		assertThat(order.getStatus()).isEqualTo(createdOrderEvent.status());
		assertThat(order.getTotalQuantity()).isEqualTo(createdOrderEvent.totalQuantity());
		assertThat(order.getRemainingQuantity()).isEqualTo(createdOrderEvent.remainingQuantity());
		assertThat(order.getPrice()).isEqualTo(createdOrderEvent.price());
		assertThat(order.getTimestamp()).isEqualTo(createdOrderEvent.timestamp());
	}

	@Test
	@DisplayName("Order Entity를 OrderCreatedEvent로 변환한다.")
	void toEvent() {
		// given
		Order order = Order.builder()
			.id(1L)
			.companyCode("005930")
			.type(Type.LIMIT_BUY)
			.status(OrderStatus.ACTIVE)
			.totalQuantity(new BigDecimal(100))
			.remainingQuantity(new BigDecimal(100))
			.price(new BigDecimal(50000))
			.timestamp(Instant.now().toEpochMilli())
			.accountId(1L)
			.build();

		// when
		CreatedOrderEvent createdOrderEvent = orderMapper.toCreatedOrderEvent(order);

		// then
		assertThat(createdOrderEvent).isNotNull();
		assertThat(createdOrderEvent.orderId()).isEqualTo(order.getId());
		assertThat(createdOrderEvent.companyCode()).isEqualTo(order.getCompanyCode());
		assertThat(createdOrderEvent.type()).isEqualTo(order.getType());
		assertThat(createdOrderEvent.status()).isEqualTo(order.getStatus());
		assertThat(createdOrderEvent.totalQuantity()).isEqualTo(order.getTotalQuantity());
		assertThat(createdOrderEvent.remainingQuantity()).isEqualTo(order.getRemainingQuantity());
	}
}
