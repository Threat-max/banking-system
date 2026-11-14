package bankingsystem.observer.subscriber;

// Author: Baimakhan Aibek (Observer)

import bankingsystem.facade.subsystem.Money;
import bankingsystem.observer.event.Transaction;
import bankingsystem.observer.event.TransactionType;

import java.util.EnumMap;
import java.util.Map;

/** Concrete observer: real-time dashboard numbers (count and volume per transaction type). */
public class TransactionStatisticsObserver implements TransactionObserver {

    private final Map<TransactionType, Integer> counts = new EnumMap<>(TransactionType.class);
    private final Map<TransactionType, Money> volumes = new EnumMap<>(TransactionType.class);

    @Override
    public void onTransaction(Transaction transaction) {
        counts.merge(transaction.type(), 1, Integer::sum);
        volumes.merge(transaction.type(), transaction.amount(), Money::plus);
    }

    public int getCount(TransactionType type) {
        return counts.getOrDefault(type, 0);
    }

    public Money getVolume(TransactionType type) {
        return volumes.getOrDefault(type, Money.ZERO);
    }

    public String summary() {
        StringBuilder text = new StringBuilder();
        for (TransactionType type : TransactionType.values()) {
            text.append(String.format("%-17s count=%d volume=%s%n",
                    type, getCount(type), getVolume(type)));
        }
        return text.toString().stripTrailing();
    }
}
