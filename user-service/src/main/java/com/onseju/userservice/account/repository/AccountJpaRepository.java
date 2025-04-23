package com.onseju.userservice.account.repository;

import com.onseju.userservice.account.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {

    @Transactional
    @Lock(value = LockModeType.OPTIMISTIC)
    Optional<Account> findByMemberId(Long memberId);
}
