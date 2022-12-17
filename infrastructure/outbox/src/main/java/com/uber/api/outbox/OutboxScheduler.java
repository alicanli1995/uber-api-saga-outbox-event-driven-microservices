package com.uber.api.outbox;

public interface OutboxScheduler {
    void processOutboxMessage();
}
