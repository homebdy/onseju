package com.onseju.userservice.holding.service;

import com.onseju.userservice.holding.domain.Holdings;
import com.onseju.userservice.holding.service.dto.AfterTradeHoldingsDto;
import com.onseju.userservice.holding.service.dto.BeforeTradeHoldingsDto;
import com.onseju.userservice.holding.service.repository.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HoldingsService {

	private final HoldingsRepository holdingsRepository;

	@Transactional
	public void updateHoldingsAfterTrade(final AfterTradeHoldingsDto params) {
		optimizeLoop(() -> {
			final Holdings holdings
					= holdingsRepository.getOrDefaultByAccountIdAndCompanyCode(params.accountId(), params.companyCode());
			holdings.updateHoldings(params.type(), params.price(), params.quantity());
			holdingsRepository.save(holdings);
		});
	}

	public void reserve(final BeforeTradeHoldingsDto dto) {
		optimizeLoop(() -> {
			if (dto.type().isSell()) {
				final Holdings holdings
						= holdingsRepository.getByAccountIdAndCompanyCode(dto.accountId(), dto.companyCode());
				holdings.validateExistHoldings();
				holdings.validateEnoughHoldings(dto.totalQuantity());
				holdings.reserveOrder(dto.totalQuantity());
				holdingsRepository.save(holdings);
			}
		});
	}

	private void optimizeLoop(Runnable run) {
		while (true) {
			try {
				run.run();
				break;
			} catch (ObjectOptimisticLockingFailureException ex) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
