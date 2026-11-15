package bankingsystem.decorator.concrete;

// Author: Urazbek Bek (Decorator)

import bankingsystem.decorator.base.AccountDecorator;
import bankingsystem.facade.exception.TransactionLimitExceededException;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.factorymethod.product.Account;

/** Security feature: rejects any single withdrawal above a limit. */
public class PerTransactionLimitDecorator extends AccountDecorator {

    private final Money limit;

    public PerTransactionLimitDecorator(Account wrapped, Money limit) {
        super(wrapped);
        this.limit = limit.requirePositive("Transaction limit");
    }

    @Override
    public void withdraw(Money amount) {
        if (amount.isGreaterThan(limit)) {
            throw new TransactionLimitExceededException("Withdrawal " + amount
                    + " exceeds the per-transaction limit " + limit + " of " + getId());
        }
        wrapped.withdraw(amount);
    }

    @Override
    public String describe() {
        return wrapped.describe() + " + per-transaction limit " + limit;
    }
}
