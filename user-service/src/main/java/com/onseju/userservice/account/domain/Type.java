package com.onseju.userservice.account.domain;

public enum Type {

    LIMIT_BUY,
    LIMIT_SELL,
    MARKET_BUY,
    MARKET_SELL;

    public boolean isSell() {
        return this == LIMIT_SELL || this == MARKET_SELL;
    }

    public boolean isBuy() {
        return this == LIMIT_BUY || this == MARKET_BUY;
    }
}
