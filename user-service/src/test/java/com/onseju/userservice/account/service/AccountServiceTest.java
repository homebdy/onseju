package com.onseju.userservice.account.service;

import com.onseju.userservice.account.domain.Account;
import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.exception.AccountNotFoundException;
import com.onseju.userservice.account.exception.InsufficientBalanceException;
import com.onseju.userservice.account.service.dto.AfterTradeAccountDto;
import com.onseju.userservice.account.service.dto.BeforeTradeAccountDto;
import com.onseju.userservice.fake.FakeAccountRepository;
import com.onseju.userservice.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceTest {

	AccountService accountService;
	FakeAccountRepository fakeAccountRepository = new FakeAccountRepository();

	private final Member member = Member.builder()
			.id(100L)
			.email("test@example.com")
			.username("testuser")
			.build();

	@BeforeEach
	void setUp() {
		accountService = new AccountService(fakeAccountRepository);
		member.createAccount();
		fakeAccountRepository.save(member.getAccount());
	}

	@Nested
	@DisplayName("체결 이후 잔액 및 예약금 업데이트")
	class AccountAfterTrade {
		@Test
		@DisplayName("매수 요청시, account에서 예약금을 저장한다.")
		void updateAccountAfterBuyTradeSuccess() {
			// given
			Long accountId = 1L;
			AfterTradeAccountDto dto = new AfterTradeAccountDto(1L, Type.BUY, BigDecimal.ONE, BigDecimal.ONE);

			// when
			accountService.updateAccountAfterTrade(dto);

			// then
			Account account = fakeAccountRepository.getById(accountId);
			assertThat(account.getBalance()).isEqualTo(new BigDecimal(100000000).subtract(BigDecimal.ONE));
			assertThat(account.getReservedBalance().abs()).isEqualTo(BigDecimal.ONE);
		}

		@Test
		@DisplayName("매도 요청시, account에서 금액을 추가한다.")
		void updateAccountAfterSellTradeSuccess() {
			// given
			Long accountId = 1L;
			AfterTradeAccountDto params = new AfterTradeAccountDto(1L, Type.SELL, BigDecimal.ONE, BigDecimal.ONE);

			// when
			accountService.updateAccountAfterTrade(params);

			// then
			Account account = fakeAccountRepository.getById(accountId);
			assertThat(account.getBalance()).isEqualTo(new BigDecimal(100000001));
		}
	}

	@Nested
	@DisplayName("주문 저장 이전 validation 및 예약금 저장")
	class AccountValidationAndReservation {

		@Test
		@DisplayName("정상적으로 동작할 경우 Account id를 반환한다.")
		void getAccountId() {
			// given
			BeforeTradeAccountDto dto = getOrderValidationRequest(member.getId(), Type.BUY, new BigDecimal(1000));

			// when
			Long accountId = accountService.reserve(dto);

			// then
			assertThat(accountId).isNotNull();
		}

		@Test
		@DisplayName("매도 주문의 경우 업데이트가 발생하지 않는다.")
		void updateNothingForSellOrder() {
			// given
			BeforeTradeAccountDto request = getOrderValidationRequest(member.getId(), Type.SELL, new BigDecimal(1000));
			Account before = fakeAccountRepository.getByMemberId(member.getId());
			Long accountId = before.getId();
			BigDecimal beforeBalance = before.getBalance();
			BigDecimal beforeReservedBalance = before.getReservedBalance();

			// when
			accountService.reserve(request);

			// then
			Account afterAccount = fakeAccountRepository.getById(accountId);
			assertThat(accountId).isEqualTo(afterAccount.getId());
			assertThat(beforeBalance).isEqualTo(afterAccount.getBalance());
			assertThat(beforeReservedBalance).isEqualTo(afterAccount.getReservedBalance());
		}

		@Test
		@DisplayName("매수 주문의 경우 예약금(reserved balance)를 추가한다.")
		void updateReservedBalanceForBuyOrder() {
			// given
			BigDecimal price = new BigDecimal(1000);
			BeforeTradeAccountDto request = getOrderValidationRequest(member.getId(), Type.BUY, price);

			Account before = fakeAccountRepository.getByMemberId(member.getId());
			Long accountId = before.getId();
			BigDecimal beforeBalance = before.getBalance();
			BigDecimal beforeReservedBalance = before.getReservedBalance();

			// when
			accountService.reserve(request);

			// then
			Account afterAccount = fakeAccountRepository.getById(accountId);
			assertThat(beforeBalance).isEqualTo(afterAccount.getBalance());
			assertThat(beforeReservedBalance.add(request.price().multiply(request.totalQuantity())))
					.isEqualTo(afterAccount.getReservedBalance());
		}

		@Test
		@DisplayName("매수 주문의 경우 남은 금액이 주문 금액보다 적을 경우 예외가 발생한다.")
		void throwExceptionWhenInsufficientBalance() {
			// given
			Account before = fakeAccountRepository.getByMemberId(member.getId());
			BeforeTradeAccountDto request = getOrderValidationRequest(member.getId(), Type.BUY, before.getBalance().add(BigDecimal.ONE));

			// when, then
			assertThatThrownBy(() -> accountService.reserve(request))
					.isInstanceOf(InsufficientBalanceException.class);
		}

		@Test
		@DisplayName("")
		void throwNotFoundExceptionWhenInvalidMemberId() {
			// given
			Long memberId = Long.MAX_VALUE;
			BeforeTradeAccountDto request = getOrderValidationRequest(memberId, Type.BUY, new BigDecimal(1000));

			// when, then
			assertThatThrownBy(() -> accountService.reserve(request))
					.isInstanceOf(AccountNotFoundException.class);
		}

		private BeforeTradeAccountDto getOrderValidationRequest(Long memberId, Type type, BigDecimal price) {
			return new BeforeTradeAccountDto(
					memberId,
					type,
					price,
					BigDecimal.ONE
			);
		}
	}
}
