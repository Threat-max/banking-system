package bankingsystem.factorymethod.product;

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;

/**
 * Component interface shared by real accounts and by decorators.
 * Clients (the facade) only see this type.
 */
public interface Account {

    String getId();

    Customer getOwner();

    Money getBalance();

    void deposit(Money amount);

    void withdraw(Money amount);

    Money getMonthlyFee();

    String describe();

    void setInterestStrategy(InterestStrategy strategy);

    /** Interest this account would earn in the given number of months (delegated to the strategy). */
    Money calculateInterest(int months);

    String describeInterest();
}
