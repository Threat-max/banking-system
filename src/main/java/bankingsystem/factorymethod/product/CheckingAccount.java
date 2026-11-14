package bankingsystem.factorymethod.product;

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.concrete.NoInterestStrategy;

/** Concrete product: everyday account, no interest, small monthly fee. */
public class CheckingAccount extends BankAccount {

    private static final Money MONTHLY_FEE = Money.of("5.00");

    public CheckingAccount(String id, Customer owner) {
        super(id, owner, new NoInterestStrategy());
    }

    @Override
    public Money getMonthlyFee() {
        return MONTHLY_FEE;
    }

    @Override
    public String describe() {
        return "Checking account";
    }
}
