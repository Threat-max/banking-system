package bankingsystem.observer.subscriber;

// Author: Baimakhan Aibek (Observer)

import bankingsystem.observer.event.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Concrete observer: keeps an append-only ledger line for every transaction. */
public class AuditLogObserver implements TransactionObserver {

    private final List<String> entries = new ArrayList<>();

    @Override
    public void onTransaction(Transaction transaction) {
        entries.add(transaction.toString());
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
