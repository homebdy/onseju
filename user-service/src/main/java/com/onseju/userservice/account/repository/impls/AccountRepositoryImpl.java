package com.onseju.userservice.account.repository.impls;

import com.onseju.userservice.account.domain.Account;
import com.onseju.userservice.account.exception.AccountNotFoundException;
import com.onseju.userservice.account.repository.AccountJpaRepository;
import com.onseju.userservice.account.service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

	private final AccountJpaRepository accountJpaRepository;

	@Override
	public Account getById(final Long id) {
		return accountJpaRepository.findById(id)
				.orElseThrow(AccountNotFoundException::new);
	}

	@Override
	public void save(final Account account) {
		accountJpaRepository.save(account);
	}

	@Override
	public Account getByMemberId(Long memberId) {
		return accountJpaRepository.findByMemberId(memberId)
				.orElseThrow(AccountNotFoundException::new);
	}
}
