package com.onseju.userservice.account.service.repository;

import com.onseju.userservice.account.domain.Account;

public interface AccountRepository {

	Account getById(final Long id);

	void save(final Account account);

	Account getByMemberId(final Long memberId);
}
