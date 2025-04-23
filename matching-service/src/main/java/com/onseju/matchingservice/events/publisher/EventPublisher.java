package com.onseju.matchingservice.events.publisher;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher<T> {
    CompletableFuture<Void> publishEvent(T event);
}