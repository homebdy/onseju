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
	public Holdings getByMemberIdAndCompanyCode(final Long memberId, final String companyCode) {
		return holdingsJpaRepository.findByMemberIdAndCompanyCode(memberId, companyCode)
				.orElseThrow(HoldingsNotFoundException::new);
	}

	@Override
	public Holdings getOrDefaultByMemberIdAndCompanyCode(final Long memberId, final String companyCode) {
		return holdingsJpaRepository.findByMemberIdAndCompanyCode(memberId, companyCode)
				.orElse(
						Holdings.builder()
								.memberId(memberId)
								.companyCode(companyCode)
								.quantity(BigDecimal.ZERO)
								.reservedQuantity(BigDecimal.ZERO)
								.averagePrice(BigDecimal.ZERO)
								.totalPurchasePrice(BigDecimal.ZERO)
								.build()
				);
	}
}
