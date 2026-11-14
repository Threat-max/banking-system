package bankingsystem.observer.event;

import bankingsystem.facade.subsystem.Money;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Immutable record of one completed money movement. This is the event sent to observers. */
public record Transaction(String id, String accountId, TransactionType type,
                          Money amount, LocalDateTime timestamp) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String toString() {
        return String.format("%s | %s | %-9s | %-17s | %s",
                id, timestamp.format(FORMAT), accountId, type, amount);
    }
}
