package com.onseju.userservice.holding.repository;

import com.onseju.userservice.holding.domain.Holdings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface HoldingsJpaRepository extends JpaRepository<Holdings, Long> {

	@Lock(LockModeType.OPTIMISTIC)
	@Transactional
	Optional<Holdings> findByAccountIdAndCompanyCode(final Long accountId, final String companyCode);
}
