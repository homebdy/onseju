package com.onseju.userservice.order;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.exception.InsufficientBalanceException;
import com.onseju.userservice.account.mapper.AccountMapper;
import com.onseju.userservice.account.service.AccountService;
import com.onseju.userservice.account.service.dto.BeforeTradeAccountDto;
import com.onseju.userservice.grpc.GrpcValidateRequest;
import com.onseju.userservice.grpc.GrpcValidateResponse;
import com.onseju.userservice.holding.mapper.HoldingsMapper;
import com.onseju.userservice.holding.service.HoldingsService;
import com.onseju.userservice.holding.service.dto.BeforeTradeHoldingsDto;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReservationServiceTest {

	@Mock
	private AccountService accountService;

	@Mock
	private AccountMapper accountMapper;

	@Mock
	private HoldingsService holdingsService;

	@Mock
	private HoldingsMapper holdingsMapper;

	@InjectMocks
	private OrderReservationService orderReservationService;

	@Test
	void validateOrder_Success() {
		// Given
		GrpcValidateRequest request = GrpcValidateRequest.newBuilder()
			.setCompanyCode("AAPL")
			.setType("LIMIT_BUY")
			.setTotalQuantity("10")
			.setPrice("150.5")
			.setMemberId(123L)
			.build();

		BeforeTradeOrderDto dto = new BeforeTradeOrderDto("AAPL", "LIMIT_BUY",
			new BigDecimal("10"), new BigDecimal("150.5"), 123L,1L);

		BeforeTradeAccountDto accountDto = BeforeTradeAccountDto.builder()
			.memberId(123456L)
			.type(Type.BUY)  // 또는 Type.SELL
			.price(new BigDecimal("150.50"))
			.totalQuantity(new BigDecimal("10"))
			.build();

		BeforeTradeHoldingsDto holdingsDto = BeforeTradeHoldingsDto.builder()
			.type(Type.SELL)
			.accountId(789012L)
			.companyCode("AAPL")
			.totalQuantity(new BigDecimal("5"))
			.build();

		when(accountMapper.toBeforeTradeAccountDto(any(), any())).thenReturn(accountDto);
		when(holdingsMapper.toBeforeTradeHoldingsDto(any(), any(), any())).thenReturn(holdingsDto);
		when(accountService.reserve(any())).thenReturn(456L);

		StreamObserver<GrpcValidateResponse> responseObserver = mock(StreamObserver.class);

		// When
		orderReservationService.validateOrder(request, responseObserver);

		// Then
		ArgumentCaptor<GrpcValidateResponse> responseCaptor = ArgumentCaptor.forClass(GrpcValidateResponse.class);
		verify(responseObserver).onNext(responseCaptor.capture());
		verify(responseObserver).onCompleted();

		GrpcValidateResponse response = responseCaptor.getValue();
		assertEquals(456L, response.getAccountId());
		assertTrue(response.getResult());
	}

	@Test
	void validateOrder_InsufficientBalance() {
		// Given
		GrpcValidateRequest request = GrpcValidateRequest.newBuilder()
			.setCompanyCode("AAPL")
			.setType("LIMIT_BUY")
			.setTotalQuantity("10")
			.setPrice("150.5")
			.setMemberId(123L)
			.build();


		when(accountService.reserve(any())).thenThrow(new InsufficientBalanceException());

		StreamObserver<GrpcValidateResponse> responseObserver = mock(StreamObserver.class);

		// When
		orderReservationService.validateOrder(request, responseObserver);

		// Then
		ArgumentCaptor<GrpcValidateResponse> responseCaptor = ArgumentCaptor.forClass(GrpcValidateResponse.class);
		verify(responseObserver).onNext(responseCaptor.capture());
		verify(responseObserver).onCompleted();

		GrpcValidateResponse response = responseCaptor.getValue();
		assertFalse(response.getResult());
		assertEquals("잔액이 부족합니다", response.getMessage());
	}
}

