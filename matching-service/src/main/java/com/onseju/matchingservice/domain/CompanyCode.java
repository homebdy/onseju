package com.onseju.matchingservice.domain;

import java.util.Objects;

public class CompanyCode {

    private final String companyCode;

    public CompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompanyCode that = (CompanyCode) o;
        return companyCode.equals(that.companyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyCode);
    }

    public String getCompanyCode() {
        return companyCode;
    }
}
