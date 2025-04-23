package com.onseju.userservice.account.domain;

public enum Type {

    BUY,
    SELL;

    public boolean isSell() {
        return this == SELL;
    }

    public boolean isBuy() {
        return this == BUY;
    }
}
