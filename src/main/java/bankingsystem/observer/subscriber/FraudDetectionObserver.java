package bankingsystem.observer.subscriber;

// Author: Baimakhan Aibek (Observer)

import bankingsystem.facade.subsystem.Money;
import bankingsystem.observer.event.FraudAlert;
import bankingsystem.observer.event.Transaction;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Concrete observer: raises an alert for suspicious outgoing transactions.
 * Rule 1: a single transaction at or above the threshold.
 * Rule 2: several outgoing transactions on one account in a short window.
 */
public class FraudDetectionObserver implements TransactionObserver {

    private static final Money LARGE_AMOUNT_THRESHOLD = Money.of(5_000);
    private static final int RAPID_TRANSACTION_COUNT = 3;
    private static final Duration RAPID_WINDOW = Duration.ofMinutes(10);

    private final Consumer<FraudAlert> alertHandler;
    private final List<FraudAlert> alerts = new ArrayList<>();
    private final Map<String, Deque<LocalDateTime>> recentOutgoingByAccount = new HashMap<>();

    public FraudDetectionObserver(Consumer<FraudAlert> alertHandler) {
        this.alertHandler = Objects.requireNonNull(alertHandler, "alertHandler");
    }

    @Override
    public void onTransaction(Transaction transaction) {
        if (!transaction.type().isOutgoing()) {
            return;
        }
        if (!transaction.amount().isLessThan(LARGE_AMOUNT_THRESHOLD)) {
            raise(transaction, "Large transaction (threshold " + LARGE_AMOUNT_THRESHOLD + ")");
        }
        if (isRapidSuccession(transaction)) {
            raise(transaction, RAPID_TRANSACTION_COUNT + " outgoing transactions within "
                    + RAPID_WINDOW.toMinutes() + " minutes");
        }
    }

    public List<FraudAlert> getAlerts() {
        return Collections.unmodifiableList(alerts);
    }

    private boolean isRapidSuccession(Transaction transaction) {
        Deque<LocalDateTime> recent = recentOutgoingByAccount
                .computeIfAbsent(transaction.accountId(), accountId -> new ArrayDeque<>());
        LocalDateTime windowStart = transaction.timestamp().minus(RAPID_WINDOW);
        while (!recent.isEmpty() && recent.peekFirst().isBefore(windowStart)) {
            recent.pollFirst();
        }
        recent.addLast(transaction.timestamp());
        return recent.size() >= RAPID_TRANSACTION_COUNT;
    }

    private void raise(Transaction transaction, String reason) {
        FraudAlert alert = new FraudAlert(transaction, reason);
        alerts.add(alert);
        alertHandler.accept(alert);
    }
}
