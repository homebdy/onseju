package com.onseju.userservice.member.domain;

import com.onseju.userservice.account.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class MemberTest {

    @Test
    @DisplayName("Successfully create account with initial balance")
    void successfullyCreateAccount() {
        // given
        Member member = Member.builder()
                .googleId("123456")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        // when
        member.createAccount();

        // then
        assertThat(member.getAccount())
                .isNotNull()
                .extracting(Account::getMember)
                .isEqualTo(member);

        assertThat(member.getAccount().getBalance())
                .isEqualTo(new BigDecimal("100000000"));
    }

	@Test
    @DisplayName("Create account with proper bidirectional relationship")
    void createAccountWithProperBidirectionalRelationship() {
        // given
        Member member = Member.builder()
                .googleId("123456")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        // when
        member.createAccount();

        // then
        Account createdAccount = member.getAccount();
        assertThat(createdAccount.getMember())
                .isSameAs(member);  // 양방향 관계 검증
    }

}
