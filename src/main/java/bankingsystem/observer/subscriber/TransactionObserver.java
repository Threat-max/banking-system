package bankingsystem.observer.subscriber;

// Author: Baimakhan Aibek (Observer)

import bankingsystem.observer.event.Transaction;

/** Observer interface: anything that wants to react to completed transactions. */
public interface TransactionObserver {

    void onTransaction(Transaction transaction);
}
