package bankingsystem.factorymethod.product;

import bankingsystem.facade.exception.InsufficientFundsException;
import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;

import java.util.Objects;

/** Shared state and rules of a plain account. Interest is delegated to a strategy. */
public abstract class BankAccount implements Account {

    private final String id;
    private final Customer owner;
    private Money balance = Money.ZERO;
    private InterestStrategy interestStrategy;

    protected BankAccount(String id, Customer owner, InterestStrategy interestStrategy) {
        this.id = Objects.requireNonNull(id, "id");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.interestStrategy = Objects.requireNonNull(interestStrategy, "interestStrategy");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Customer getOwner() {
        return owner;
    }

    @Override
    public Money getBalance() {
        return balance;
    }

    @Override
    public void deposit(Money amount) {
        balance = balance.plus(amount.requirePositive("Deposit amount"));
    }

    @Override
    public void withdraw(Money amount) {
        amount.requirePositive("Withdrawal amount");
        if (amount.isGreaterThan(balance)) {
            throw new InsufficientFundsException("Insufficient funds in " + id
                    + ": balance " + balance + ", requested " + amount);
        }
        balance = balance.minus(amount);
    }

    @Override
    public void setInterestStrategy(InterestStrategy strategy) {
        this.interestStrategy = Objects.requireNonNull(strategy, "strategy");
    }

    @Override
    public Money calculateInterest(int months) {
        return interestStrategy.calculateInterest(balance, months);
    }

    @Override
    public String describeInterest() {
        return interestStrategy.describe();
    }
}
