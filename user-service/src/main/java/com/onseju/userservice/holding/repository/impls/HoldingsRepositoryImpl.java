package com.onseju.userservice.holding.repository.impls;

import com.onseju.userservice.holding.domain.Holdings;
import com.onseju.userservice.holding.exception.HoldingsNotFoundException;
import com.onseju.userservice.holding.repository.HoldingsJpaRepository;
import com.onseju.userservice.holding.service.repository.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class HoldingsRepositoryImpl implements HoldingsRepository {

	private final HoldingsJpaRepository holdingsJpaRepository;

	@Override
	public Holdings save(final Holdings holdings) {
		return holdingsJpaRepository.save(holdings);
	}

	@Override
	public Holdings getByAccountIdAndCompanyCode(final Long accountId, final String companyCode) {
		return holdingsJpaRepository.findByAccountIdAndCompanyCode(accountId, companyCode)
				.orElseThrow(HoldingsNotFoundException::new);
	}

	@Override
	public Holdings getOrDefaultByAccountIdAndCompanyCode(final Long accountId, final String companyCode) {
		return holdingsJpaRepository.findByAccountIdAndCompanyCode(accountId, companyCode)
				.orElse(
						Holdings.builder()
								.accountId(accountId)
								.companyCode(companyCode)
								.quantity(BigDecimal.ZERO)
								.reservedQuantity(BigDecimal.ZERO)
								.averagePrice(BigDecimal.ZERO)
								.totalPurchasePrice(BigDecimal.ZERO)
								.build()
				);
	}
}
