package bankingsystem.observer.subject;

// Author: Baimakhan Aibek (Observer)

import bankingsystem.observer.event.Transaction;
import bankingsystem.observer.subscriber.TransactionObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Subject of the Observer pattern. It knows observers only through their interface. */
public class TransactionMonitor {

    private final List<TransactionObserver> observers = new ArrayList<>();

    public void subscribe(TransactionObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer"));
    }

    public void unsubscribe(TransactionObserver observer) {
        observers.remove(observer);
    }

    public void publish(Transaction transaction) {
        for (TransactionObserver observer : List.copyOf(observers)) {
            observer.onTransaction(transaction);
        }
    }
}
