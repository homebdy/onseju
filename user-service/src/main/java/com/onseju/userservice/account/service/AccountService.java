package com.onseju.userservice.account.service;

import com.onseju.userservice.account.domain.Account;
import com.onseju.userservice.account.service.dto.AfterTradeAccountDto;
import com.onseju.userservice.account.service.dto.BeforeTradeAccountDto;
import com.onseju.userservice.account.service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

@RequiredArgsConstructor
@Service
@Slf4j
public class AccountService {

	private final AccountRepository accountRepository;

	public void updateAccountAfterTrade(final AfterTradeAccountDto dto) {
		optimizeLoop(() -> {
			Account account = accountRepository.getById(dto.accountId());
			account.processOrder(
					dto.type(),
					dto.price(),
					dto.quantity()
			);
			accountRepository.save(account);
			return account.getId();
		});
	}

	public Long reserve(final BeforeTradeAccountDto dto) {
		return optimizeLoop(() -> {
			if (dto.type().isBuy()) {
				Account account = accountRepository.getByMemberId(dto.memberId());
				account.validateDepositBalance(dto.price().multiply(dto.totalQuantity()));
				account.processReservedOrder(dto.price().multiply(dto.totalQuantity()));
				accountRepository.save(account);
				return account.getId();
			}
			return accountRepository.getByMemberId(dto.memberId()).getId();
		});
	}


	private Long optimizeLoop(LongSupplier supplier) {
		while (true) {
			AtomicInteger repeat = new AtomicInteger();
			try {
				return supplier.getAsLong();
			} catch (ObjectOptimisticLockingFailureException ex) {
				try {
					Thread.sleep((long) Math.pow(200, repeat.getAndIncrement()));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
