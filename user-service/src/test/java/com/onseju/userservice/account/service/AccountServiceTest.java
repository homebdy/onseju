package com.onseju.userservice.account.service;

import com.onseju.userservice.account.domain.Account;
import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.exception.AccountNotFoundException;
import com.onseju.userservice.account.exception.InsufficientBalanceException;
import com.onseju.userservice.account.service.dto.CreatedOrderAccountUpdateDto;
import com.onseju.userservice.events.dto.MatchedOrderUpdateEvent;
import com.onseju.userservice.fake.FakeAccountRepository;
import com.onseju.userservice.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceTest {

	AccountService accountService;
	FakeAccountRepository fakeAccountRepository = new FakeAccountRepository();

	private Long ACCOUNT_ID;
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
		ACCOUNT_ID = fakeAccountRepository.getByMemberId(member.getId()).getId();
	}

	@Nested
	@DisplayName("체결 이후 잔액 및 예약금 업데이트")
	class AccountAfterTrade {

		@Test
		@DisplayName("매수 요청시, account에서 예약금을 저장한다.")
		void updateAccountAfterBuyTradeSuccess() {
			// given
			MatchedOrderUpdateEvent event = new MatchedOrderUpdateEvent(UUID.randomUUID(), Type.LIMIT_BUY, "005930", member.getId(), new BigDecimal(10), new BigDecimal(1000), Instant.now().getEpochSecond());

			// when
			accountService.updateAccountAfterTrade(event);

			// then
			Account account = fakeAccountRepository.getById(ACCOUNT_ID);
			assertThat(account.getBalance()).isEqualTo(new BigDecimal(100000000).subtract(event.price().multiply(event.quantity())));
			assertThat(account.getReservedBalance().abs()).isEqualTo(event.price().multiply(event.quantity()));
		}

		@Test
		@DisplayName("매도 요청시, account에서 금액을 추가한다.")
		void updateAccountAfterSellTradeSuccess() {
			// given
			MatchedOrderUpdateEvent event = new MatchedOrderUpdateEvent(UUID.randomUUID(), Type.LIMIT_SELL, "005930", member.getId(), new BigDecimal(10), new BigDecimal(1000), Instant.now().getEpochSecond());

			// when
			accountService.updateAccountAfterTrade(event);

			// then
			Account account = fakeAccountRepository.getById(ACCOUNT_ID);
			assertThat(account.getBalance()).isEqualTo(new BigDecimal(100000000).add(event.price().multiply(event.quantity())));
		}
	}

	@Nested
	@DisplayName("주문 저장 이전 validation 및 예약금 저장")
	class AccountValidationAndReservation {

		@Test
		@DisplayName("정상적으로 동작할 경우 예외가 없이 동작한다.")
		void getAccountId() {
			// given
			CreatedOrderAccountUpdateDto dto = getOrderValidationRequest(member.getId(), Type.LIMIT_BUY, new BigDecimal(1000));

			// when, then
			assertThatNoException().isThrownBy(() -> accountService.reserve(dto));
		}

		@Test
		@DisplayName("매도 주문의 경우 업데이트가 발생하지 않는다.")
		void updateNothingForSellOrder() {
			// given
			CreatedOrderAccountUpdateDto request = getOrderValidationRequest(member.getId(), Type.LIMIT_SELL, new BigDecimal(1000));
			Account before = fakeAccountRepository.getByMemberId(member.getId());
			Long accountId = before.getId();
			BigDecimal beforeBalance = before.getBalance();
			BigDecimal beforeReservedBalance = before.getReservedBalance();

			// when
			accountService.reserve(request);

			// then
			Account afterAccount = fakeAccountRepository.getById(ACCOUNT_ID);
			assertThat(accountId).isEqualTo(afterAccount.getId());
			assertThat(beforeBalance).isEqualTo(afterAccount.getBalance());
			assertThat(beforeReservedBalance).isEqualTo(afterAccount.getReservedBalance());
		}

		@Test
		@DisplayName("매수 주문의 경우 예약금(reserved balance)를 추가한다.")
		void updateReservedBalanceForBuyOrder() {
			// given
			BigDecimal price = new BigDecimal(1000);
			CreatedOrderAccountUpdateDto request = getOrderValidationRequest(member.getId(), Type.LIMIT_BUY, price);

			Account before = fakeAccountRepository.getByMemberId(member.getId());
			BigDecimal beforeBalance = before.getBalance();
			BigDecimal beforeReservedBalance = before.getReservedBalance();

			// when
			accountService.reserve(request);

			// then
			Account afterAccount = fakeAccountRepository.getById(ACCOUNT_ID);
			assertThat(beforeBalance).isEqualTo(afterAccount.getBalance());
			assertThat(beforeReservedBalance.add(request.price().multiply(request.totalQuantity())))
					.isEqualTo(afterAccount.getReservedBalance());
		}

		@Test
		@DisplayName("매수 주문의 경우 남은 금액이 주문 금액보다 적을 경우 예외가 발생한다.")
		void throwExceptionWhenInsufficientBalance() {
			// given
			Account before = fakeAccountRepository.getByMemberId(member.getId());
			CreatedOrderAccountUpdateDto request = getOrderValidationRequest(member.getId(), Type.LIMIT_BUY, before.getBalance().add(BigDecimal.ONE));

			// when, then
			assertThatThrownBy(() -> accountService.reserve(request))
					.isInstanceOf(InsufficientBalanceException.class);
		}

		@Test
		@DisplayName("")
		void throwNotFoundExceptionWhenInvalidMemberId() {
			// given
			Long memberId = Long.MAX_VALUE;
			CreatedOrderAccountUpdateDto request = getOrderValidationRequest(memberId, Type.LIMIT_BUY, new BigDecimal(1000));

			// when, then
			assertThatThrownBy(() -> accountService.reserve(request))
					.isInstanceOf(AccountNotFoundException.class);
		}

		private CreatedOrderAccountUpdateDto getOrderValidationRequest(Long memberId, Type type, BigDecimal price) {
			return new CreatedOrderAccountUpdateDto(
					memberId,
					type,
					price,
					BigDecimal.ONE
			);
		}
	}
}
