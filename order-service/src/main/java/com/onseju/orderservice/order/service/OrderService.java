package com.onseju.orderservice.order.service;

import com.onseju.orderservice.company.domain.Company;
import com.onseju.orderservice.company.service.repository.CompanyRepository;
import com.onseju.orderservice.events.dto.CreatedOrderEvent;
import com.onseju.orderservice.events.dto.MatchedOrderUpdateEvent;
import com.onseju.orderservice.events.publisher.EventPublisher;
import com.onseju.orderservice.global.utils.TsidGenerator;
import com.onseju.orderservice.order.client.UserServiceClient;
import com.onseju.orderservice.order.controller.resposne.OrderResponse;
import com.onseju.orderservice.order.domain.Order;
import com.onseju.orderservice.order.dto.MatchedOrderUpdateDto;
import com.onseju.orderservice.order.dto.OrderValidationResponse;
import com.onseju.orderservice.order.exception.OrderNotValidateException;
import com.onseju.orderservice.order.mapper.OrderMapper;
import com.onseju.orderservice.order.service.dto.OrderCreateCommand;
import com.onseju.orderservice.order.service.repository.OrderRepository;
import com.onseju.orderservice.order.service.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final CompanyRepository companyRepository;
	private final EventPublisher<CreatedOrderEvent> orderEventPublisher;
	private final EventPublisher<MatchedOrderUpdateEvent> matchedEventPublisher;
	private final UserServiceClient userServiceClient;
	private final OrderMapper orderMapper;
	private final TsidGenerator tsidGenerator;

	public OrderResponse placeOrder(final OrderCreateCommand command) {
		// 주문 유효성 검증
		validateOrder(command.price(), command.companyCode());

		// 계좌 및 보유 주식 검증(REST 요청)
		Long memberId = getMemberIdFromUserService(command);

		// 주문 생성 이벤트 발행
		final Order order = orderMapper.toEntity(tsidGenerator.nextId(), command, memberId);
		orderEventPublisher.publishEvent(orderMapper.toCreatedOrderEvent(order));

		return orderMapper.toOrderResponse(order);
	}

	// 주문 유효성 검증
	private void validateOrder(final BigDecimal price, final String companyCode) {
		Company company = companyRepository.findByIsuSrtCd(companyCode);
		OrderValidator.validateOrder(price, company);
	}

	// 외부의 user-service와 rest 통신
	private Long getMemberIdFromUserService(final OrderCreateCommand command) {
		OrderValidationResponse response = userServiceClient.validateOrder(command);

		if (!response.result()) {
			throw new OrderNotValidateException();
		}

		return response.memberId();
	}

	/**
	 * 주문 예약 수량 업데이트
	 */
	@Transactional
	public void updateRemainingQuantity(final MatchedOrderUpdateDto dto) {
		final Order order = orderRepository.getById(dto.orderId());
		order.decreaseRemainingQuantity(dto.quantity());
		matchedEventPublisher.publishEvent(orderMapper.toMatchedOrderUpdateEvent(order, dto));
	}

	@Transactional
	public void save(final CreatedOrderEvent event) {
		final Order order = orderMapper.toEntity(event);
		orderRepository.save(order);
	}
}
