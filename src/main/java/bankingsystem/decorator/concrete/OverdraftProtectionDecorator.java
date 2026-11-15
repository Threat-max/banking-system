package bankingsystem.decorator.concrete;

// Author: Urazbek Bek (Decorator)

import bankingsystem.decorator.base.AccountDecorator;
import bankingsystem.facade.exception.InsufficientFundsException;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.factorymethod.product.Account;

/**
 * Lets the account go below zero up to a limit. The wrapped account is never
 * touched in a forbidden way: the decorator tracks the overdrawn amount itself
 * and reports balance = wrapped balance - overdrawn.
 */
public class OverdraftProtectionDecorator extends AccountDecorator {

    private static final Money MONTHLY_FEE = Money.of("3.00");

    private final Money limit;
    private Money overdrawn = Money.ZERO;

    public OverdraftProtectionDecorator(Account wrapped, Money limit) {
        super(wrapped);
        this.limit = limit.requirePositive("Overdraft limit");
    }

    @Override
    public Money getBalance() {
        return wrapped.getBalance().minus(overdrawn);
    }

    @Override
    public void deposit(Money amount) {
        amount.requirePositive("Deposit amount");
        Money repayment = amount.min(overdrawn);
        overdrawn = overdrawn.minus(repayment);
        Money remainder = amount.minus(repayment);
        if (remainder.isPositive()) {
            wrapped.deposit(remainder);
        }
    }

    @Override
    public void withdraw(Money amount) {
        amount.requirePositive("Withdrawal amount");
        Money available = wrapped.getBalance();
        if (!amount.isGreaterThan(available)) {
            wrapped.withdraw(amount);
            return;
        }
        Money shortfall = amount.minus(available);
        if (overdrawn.plus(shortfall).isGreaterThan(limit)) {
            throw new InsufficientFundsException("Overdraft limit " + limit + " exceeded in "
                    + getId() + ": balance " + getBalance() + ", requested " + amount);
        }
        if (available.isPositive()) {
            wrapped.withdraw(available);
        }
        overdrawn = overdrawn.plus(shortfall);
    }

    @Override
    public Money getMonthlyFee() {
        return wrapped.getMonthlyFee().plus(MONTHLY_FEE);
    }

    @Override
    public String describe() {
        return wrapped.describe() + " + overdraft protection (limit " + limit + ")";
    }

    public Money getOverdrawnAmount() {
        return overdrawn;
    }
}
