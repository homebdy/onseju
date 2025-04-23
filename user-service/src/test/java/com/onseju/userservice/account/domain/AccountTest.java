package com.onseju.userservice.account.domain;

import com.onseju.userservice.account.exception.InsufficientBalanceException;
import com.onseju.userservice.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountTest {

    private final Member member = Member.builder()
            .email("test@example.com")
            .username("testuser")
            .build();

    private Account account;

    @BeforeEach
    void setUp() {
        member.createAccount();
        account = member.getAccount();
    }

    @Test
    @DisplayName("초기 금액을 1억으로 설정한다.")
    void setInitialPrice() {
        // given
        member.createAccount();
        Account account = member.getAccount();

        // when
        BigDecimal balance = account.getBalance();

        // then
        assertThat(balance).isEqualTo(new BigDecimal("100000000"));
    }

    @Test
    @DisplayName("남은 잔액이 구매 금액보다 작을 경우 예외가 발생한다.")
    void throwExceptionWhenBalanceIsNotEnough() {
        // given
        assert account.getBalance().equals(new BigDecimal("100000000")) : "Balance is not initialized(100000000)";
        BigDecimal amountToBuy = new BigDecimal("100000001");

        // when & then
        assertThatThrownBy(() -> account.validateDepositBalance(amountToBuy))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("남은 잔액이 구매 금액보다 클 경우 성공적으로 처리한다.")
    void successfullyProcessBuyOrder() {
        // given
        BigDecimal priceToBuy = new BigDecimal("30000000");
        BigDecimal quantityToBuy = new BigDecimal("1");
        BigDecimal expectedBalance = new BigDecimal("70000000");

        // when
        account.processOrder(Type.BUY, priceToBuy, quantityToBuy);

        // then
        assertThat(account.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("구매 금액을 성공적으로 처리한다.")
    void successfullyProcessSellOrder() {
        // given
        BigDecimal priceToSell = new BigDecimal("30000000");
        BigDecimal quantityToSell = new BigDecimal("1");
        BigDecimal expectedBalance = new BigDecimal("130000000");

        // when
        account.processOrder(Type.SELL, priceToSell, quantityToSell);

        // then
        assertThat(account.getBalance()).isEqualTo(expectedBalance);
    }

}