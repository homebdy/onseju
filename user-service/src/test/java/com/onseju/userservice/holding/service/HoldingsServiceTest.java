package com.onseju.userservice.holding.service;

import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.holding.domain.Holdings;
import com.onseju.userservice.holding.exception.HoldingsNotFoundException;
import com.onseju.userservice.holding.exception.InsufficientHoldingsException;
import com.onseju.userservice.fake.FakeHoldingsRepository;
import com.onseju.userservice.holding.service.dto.AfterTradeHoldingsDto;
import com.onseju.userservice.holding.service.dto.BeforeTradeHoldingsDto;
import com.onseju.userservice.holding.service.repository.HoldingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingsServiceTest {

	private static final Long ACCOUNT_ID = 1L;
	private static final String COMPANY_CODE = "005930";
	private final HoldingsRepository holdingsRepository = new FakeHoldingsRepository();
	private final HoldingsService holdingsService = new HoldingsService(holdingsRepository);

	@BeforeEach
	void setUp() {
		Holdings holdings = Holdings.builder()
				.companyCode(COMPANY_CODE)
				.quantity(new BigDecimal("100"))
				.reservedQuantity(new BigDecimal("10"))
				.averagePrice(new BigDecimal(1000))
				.totalPurchasePrice(new BigDecimal(100000))
				.accountId(ACCOUNT_ID)
				.build();
		holdingsRepository.save(holdings);
	}

	@Nested
	@DisplayName("체결 이후 체결 내용을 반영한다.")
	class AfterTrade {

		@Test
		@DisplayName("보유 내역이 존재할 경우 이전 보유 내역과 합친다.")
		void getExistingHoldings() {
			// given
			AfterTradeHoldingsDto params
					= createAfterTradeHoldingsDto(Type.SELL, new BigDecimal(1000), new BigDecimal(10));

			// when
			holdingsService.updateHoldingsAfterTrade(params);

			// then
			Holdings updatedHoldings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			assertThat(updatedHoldings.getReservedQuantity()).isEqualTo(BigDecimal.ZERO);
			assertThat(updatedHoldings.getQuantity()).isEqualTo(new BigDecimal(90));
		}

		@Test
		@DisplayName("보유 내역이 존재하지 않을 경우 새롭게 생성하여 저장한다.")
		void createHoldingsWhenHoldingsNotExist() {
			// given
			AfterTradeHoldingsDto params = new AfterTradeHoldingsDto(Type.BUY, ACCOUNT_ID, "first", new BigDecimal(1000), new BigDecimal(10));

			// when
			holdingsService.updateHoldingsAfterTrade(params);

			// then
			Holdings updatedHoldings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, "first");
			assertThat(updatedHoldings.getCompanyCode()).isEqualTo(params.companyCode());
			assertThat(updatedHoldings.getReservedQuantity()).isEqualTo(BigDecimal.ZERO);
			assertThat(updatedHoldings.getQuantity()).isEqualTo(new BigDecimal(10));
		}

		@Test
		@DisplayName("매도 주문을 업데이트 할 경우, reserved, total quantity 모두 감소시킨다.")
		void updateHoldingsForSellOrder() {
			// given
			AfterTradeHoldingsDto params = createAfterTradeHoldingsDto(Type.SELL, new BigDecimal(1000), new BigDecimal(10));
			Holdings holdings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			BigDecimal beforeReservedQuantity = holdings.getReservedQuantity();
			BigDecimal beforeQuantity = holdings.getQuantity();

			// when
			holdingsService.updateHoldingsAfterTrade(params);

			// then
			Holdings updatedHoldings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			assertThat(updatedHoldings.getCompanyCode()).isEqualTo(params.companyCode());
			assertThat(updatedHoldings.getReservedQuantity()).isEqualTo(beforeReservedQuantity.subtract(params.quantity()));
			assertThat(updatedHoldings.getQuantity()).isEqualTo(beforeQuantity.subtract(params.quantity()));
		}

		@Test
		@DisplayName("매수 주문을 업데이트 할 경우, total quantity를 증가시킨다.")
		void updateHoldingsForBuyOrder() {
			// given
			AfterTradeHoldingsDto params = createAfterTradeHoldingsDto(Type.BUY, new BigDecimal(1000), new BigDecimal(10));
			Holdings holdings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			BigDecimal beforeReservedQuantity = holdings.getReservedQuantity();
			BigDecimal beforeQuantity = holdings.getQuantity();

			// when
			holdingsService.updateHoldingsAfterTrade(params);

			// then
			Holdings updatedHoldings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			assertThat(updatedHoldings.getCompanyCode()).isEqualTo(params.companyCode());
			assertThat(updatedHoldings.getReservedQuantity()).isEqualTo(beforeReservedQuantity);
			assertThat(updatedHoldings.getQuantity()).isEqualTo(beforeQuantity.add(params.quantity()));
		}
	}

	@Nested
	@DisplayName("보유 주식 개수를 확인하고, 예약 주문 개수를 저장한다.")
	class SellOrderReservation {

		@Test
		@DisplayName("매수 주문일 경우 업데이트가 발생하지 않는다.")
		void validateHoldingsForBuyOrder() {
			// given
			BeforeTradeHoldingsDto dto = createBeforeTradeHoldingsDto(Type.BUY, BigDecimal.ONE);
			Holdings holdings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			BigDecimal beforeReservedQuantity = holdings.getReservedQuantity();
			BigDecimal beforeQuantity = holdings.getQuantity();

			// when
			holdingsService.reserve(dto);

			// then
			Holdings after = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			assertThat(after.getReservedQuantity()).isEqualTo(beforeReservedQuantity);
			assertThat(after.getQuantity()).isEqualTo(beforeQuantity);
		}

		@Test
		@DisplayName("매도 주문일 경우 reservedQuantity에 주문 수량을 추가한다.")
		void validateHoldingsForSellOrder() {
			// given
			BeforeTradeHoldingsDto request = createBeforeTradeHoldingsDto(Type.SELL, BigDecimal.ONE);
			Holdings holdings = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			BigDecimal beforeReservedQuantity = holdings.getReservedQuantity();
			BigDecimal beforeQuantity = holdings.getQuantity();

			// when
			holdingsService.reserve(request);

			// then
			Holdings after = holdingsRepository.getByAccountIdAndCompanyCode(ACCOUNT_ID, COMPANY_CODE);
			assertThat(after.getReservedQuantity()).isEqualTo(beforeReservedQuantity.add(request.totalQuantity()));
			assertThat(after.getQuantity()).isEqualTo(beforeQuantity);
		}

		@Test
		@DisplayName("매도 주문일 경우, 입력한 종목에 대한 보유 주식이 없을 경우 예외가 발생한다.")
		void throwExceptionWhenSellingStockWithoutHoldingAny() {
			// given
			BeforeTradeHoldingsDto request = new BeforeTradeHoldingsDto(
					Type.SELL,
					ACCOUNT_ID,
					"InvalidCompanyCode",
					BigDecimal.ONE
			);

			// when, then
			assertThatThrownBy(() -> holdingsService.reserve(request))
					.isInstanceOf(HoldingsNotFoundException.class);
		}

		@Test
		@DisplayName("매도 주문일 경우, 입력한 종목에 대한 보유 주식의 개수가 부족할 경우 예외가 발생한다.")
		void throwExceptionWhenSellingExceedingOwnedQuantity() {
			// given
			BeforeTradeHoldingsDto request = createBeforeTradeHoldingsDto(Type.SELL, new BigDecimal(200));
			Holdings holdings = createHoldings(new BigDecimal(10));
			holdingsRepository.save(holdings);

			// when, then
			assertThatThrownBy(() -> holdingsService.reserve(request))
					.isInstanceOf(InsufficientHoldingsException.class);
		}
	}

	private AfterTradeHoldingsDto createAfterTradeHoldingsDto(Type type, BigDecimal price, BigDecimal quantity) {
		return new AfterTradeHoldingsDto(type, ACCOUNT_ID, COMPANY_CODE, price, quantity);
	}

	private BeforeTradeHoldingsDto createBeforeTradeHoldingsDto(Type type, BigDecimal quantity) {
		return new BeforeTradeHoldingsDto(
				type,
				ACCOUNT_ID,
				COMPANY_CODE,
				quantity
		);
	}

	private Holdings createHoldings(BigDecimal quantity) {
		return Holdings.builder()
				.companyCode("005930")
				.quantity(quantity)
				.reservedQuantity(new BigDecimal(10))
				.averagePrice(new BigDecimal(1000))
				.totalPurchasePrice(new BigDecimal(10000))
				.accountId(ACCOUNT_ID)
				.build();
	}
}
