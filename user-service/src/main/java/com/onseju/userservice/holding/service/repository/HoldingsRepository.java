package com.onseju.userservice.holding.service.repository;

import com.onseju.userservice.holding.domain.Holdings;

public interface HoldingsRepository {

    Holdings getByMemberIdAndCompanyCode(final Long accountId, final String companyCode);

    Holdings save(final Holdings holdings);

    Holdings getOrDefaultByMemberIdAndCompanyCode(final Long accountId, final String companyCode);
}