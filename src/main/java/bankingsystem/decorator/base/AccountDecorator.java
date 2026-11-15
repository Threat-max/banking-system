package bankingsystem.decorator.base;

// Author: Urazbek Bek (Decorator)

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.factorymethod.product.Account;
import bankingsystem.strategy.InterestStrategy;

import java.util.Objects;

/**
 * Base decorator: implements Account and forwards every call to the wrapped account.
 * Concrete decorators override only the behavior they change.
 */
public abstract class AccountDecorator implements Account {

    protected final Account wrapped;

    protected AccountDecorator(Account wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public Customer getOwner() {
        return wrapped.getOwner();
    }

    @Override
    public Money getBalance() {
        return wrapped.getBalance();
    }

    @Override
    public void deposit(Money amount) {
        wrapped.deposit(amount);
    }

    @Override
    public void withdraw(Money amount) {
        wrapped.withdraw(amount);
    }

    @Override
    public Money getMonthlyFee() {
        return wrapped.getMonthlyFee();
    }

    @Override
    public String describe() {
        return wrapped.describe();
    }

    @Override
    public void setInterestStrategy(InterestStrategy strategy) {
        wrapped.setInterestStrategy(strategy);
    }

    @Override
    public Money calculateInterest(int months) {
        return wrapped.calculateInterest(months);
    }

    @Override
    public String describeInterest() {
        return wrapped.describeInterest();
    }
}
