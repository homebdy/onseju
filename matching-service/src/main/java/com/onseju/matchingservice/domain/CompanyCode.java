package com.onseju.matchingservice.domain;

import java.util.Objects;

public class CompanyCode {

    private final String value;

    public CompanyCode(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompanyCode that = (CompanyCode) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public String getValue() {
        return value;
    }
}
