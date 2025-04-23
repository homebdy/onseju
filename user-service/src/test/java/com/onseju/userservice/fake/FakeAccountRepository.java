package com.onseju.userservice.fake;

import com.onseju.userservice.account.domain.Account;
import com.onseju.userservice.account.exception.AccountNotFoundException;
import com.onseju.userservice.account.service.repository.AccountRepository;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

public class FakeAccountRepository implements AccountRepository {

    ConcurrentSkipListSet<Account> elements = new ConcurrentSkipListSet<>(
            Comparator.comparing(Account::getId)
    );

    @Override
    public Account getById(Long id) {
        return elements.stream()
                .filter(value -> value.getId().equals(id))
                .findAny()
                .orElseThrow(AccountNotFoundException::new);
    }

    @Override
    public Account getByMemberId(Long memberId) {
        return elements.stream()
                .filter(value -> value.getMember().getId().equals(memberId))
                .findAny()
                .orElseThrow(AccountNotFoundException::new);
    }

    public void save(Account account) {
        if (account.getId() != null) {
            Account old = getById(account.getId());
            elements.remove(old);
            elements.add(account);
        }
        Account savedAccount = Account.builder()
                .id((long) elements.size() + 1)
                .member(account.getMember())
                .balance(account.getBalance())
                .reservedBalance(account.getReservedBalance())
                .build();
        elements.add(savedAccount);
    }
}