package bankingsystem.observer.event;

// Author: Baimakhan Aibek (Observer)


public record FraudAlert(Transaction transaction, String reason) {

    @Override
    public String toString() {
        return reason + " on " + transaction.accountId() + " (" + transaction.id() + ", "
                + transaction.type() + " " + transaction.amount() + ")";
    }
}
