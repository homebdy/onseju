package com.onseju.orderservice.company.service.repository;

import com.onseju.orderservice.company.domain.Company;

import java.util.List;

public interface CompanyRepository {

    List<Company> findByIsuNmContainingOrIsuAbbrvContainingOrIsuEngNmContainingOrIsuSrtCdContaining(
            final String query);

    List<Company> findAll();

    void save(final Company company);

    void saveAll(final List<Company> companies);

    Company findByIsuSrtCd(final String isuSrt);
}
