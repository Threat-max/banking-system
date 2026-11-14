package bankingsystem.factorymethod.creator;

// Author: Tolendi Arsen (Factory Method)

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.factorymethod.product.Account;

/**
 * Creator of the Factory Method pattern.
 * openAccount() is the fixed workflow, createAccount() is the factory method
 * that each subclass overrides to decide which product is built.
 */
public abstract class AccountFactory {

    private static final int FIRST_SEQUENCE_NUMBER = 1000;

    private int sequence = FIRST_SEQUENCE_NUMBER;

    public final Account openAccount(Customer owner, Money initialDeposit) {
        Account account = createAccount(nextId(), owner);
        if (initialDeposit.isPositive()) {
            account.deposit(initialDeposit);
        }
        return account;
    }

    /** The factory method. */
    protected abstract Account createAccount(String id, Customer owner);

    protected abstract String idPrefix();

    private String nextId() {
        sequence++;
        return idPrefix() + "-" + sequence;
    }
}
