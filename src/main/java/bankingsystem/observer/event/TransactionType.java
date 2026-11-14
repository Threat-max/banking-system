package bankingsystem.observer.event;

public enum TransactionType {
    DEPOSIT(false),
    WITHDRAWAL(true),
    TRANSFER_IN(false),
    TRANSFER_OUT(true),
    LOAN_DISBURSEMENT(false);

    private final boolean outgoing;

    TransactionType(boolean outgoing) {
        this.outgoing = outgoing;
    }

    /** True when money leaves the account. Fraud rules only look at these. */
    public boolean isOutgoing() {
        return outgoing;
    }
}
