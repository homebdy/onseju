package com.onseju.userservice.integration.listener;

import com.onseju.userservice.account.domain.Account;
import com.onseju.userservice.account.domain.Type;
import com.onseju.userservice.account.mapper.AccountMapper;
import com.onseju.userservice.account.service.AccountService;
import com.onseju.userservice.account.service.repository.AccountRepository;
import com.onseju.userservice.events.dto.MatchedOrderUpdateEvent;
import com.onseju.userservice.events.listener.MatchedEventListener;
import com.onseju.userservice.holding.mapper.HoldingsMapper;
import com.onseju.userservice.holding.service.HoldingsService;
import com.onseju.userservice.member.domain.Member;
import com.onseju.userservice.member.domain.Role;
import com.onseju.userservice.member.service.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MatchedEventListenerTest {

    private static MatchedEventListenerTest instance;
    @Autowired
    AccountMapper accountMapper;
    @Autowired
    HoldingsService holdingsService;
    @Autowired
    HoldingsMapper holdingsMapper;
    @Autowired
    private MatchedEventListener matchedEventListener;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeAll
    static void setUp(@Autowired MemberRepository memberRepository) {
        instance = new MatchedEventListenerTest();
        instance.memberRepository = memberRepository;

        Member member = Member.builder()
                .email("test@example.com")
                .username("testuser")
                .googleId("testuser")
                .role(Role.USER)
                .build();
        member.createAccount();
        instance.memberRepository.save(member);

        Member member2 = Member.builder()
                .email("test2@example.com")
                .username("testuser2")
                .googleId("testuser2")
                .role(Role.USER)
                .build();
        member2.createAccount();
        instance.memberRepository.save(member2);
    }

    @Test
    @DisplayName("이벤트를 전달받아 비동기로 처리한다.")
    void handleOrderEventShouldProcessOrder() {
        // given
        MatchedOrderUpdateEvent event = new MatchedOrderUpdateEvent(
                UUID.randomUUID(),
                Type.LIMIT_BUY,
                "005930",
                1L,
                new BigDecimal(10),
                new BigDecimal(1000),
                Instant.now().getEpochSecond()
        );

        // when
        CompletableFuture.runAsync(() -> matchedEventListener.handleOrderMatched(event))

                .orTimeout(2, TimeUnit.SECONDS) // 비동기 실행을 기다림
                .join();

        // then
        Assertions.assertThatCode(() -> matchedEventListener.handleOrderMatched(event))
                .doesNotThrowAnyException();

    }

    @Test
    @DisplayName("이벤트 내용을 Account에 반영한다.")
    void updateAccounts() {
        // given
        MatchedOrderUpdateEvent event = new MatchedOrderUpdateEvent(
                UUID.randomUUID(),
                Type.LIMIT_BUY,
                "005930",
                1L,
                new BigDecimal(10),
                new BigDecimal(1000),
                Instant.now().getEpochSecond()
        );

        // when
        matchedEventListener.handleOrderMatched(event);

        // then
        Account account = accountRepository.getById(1L);

        assertThat(account.getBalance()).isEqualTo(new BigDecimal("100000000.00").subtract(event.price().multiply(event.quantity())));
    }
}
