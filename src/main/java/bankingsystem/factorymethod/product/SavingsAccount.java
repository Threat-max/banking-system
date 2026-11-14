package bankingsystem.factorymethod.product;

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;
import bankingsystem.strategy.concrete.CompoundSavingsInterestStrategy;

import java.math.BigDecimal;

/** Concrete product: earns interest, no monthly fee. */
public class SavingsAccount extends BankAccount {

    private static final BigDecimal DEFAULT_ANNUAL_RATE_PERCENT = new BigDecimal("4.0");

    public SavingsAccount(String id, Customer owner) {
        this(id, owner, new CompoundSavingsInterestStrategy(DEFAULT_ANNUAL_RATE_PERCENT));
    }

    public SavingsAccount(String id, Customer owner, InterestStrategy interestStrategy) {
        super(id, owner, interestStrategy);
    }

    @Override
    public Money getMonthlyFee() {
        return Money.ZERO;
    }

    @Override
    public String describe() {
        return "Savings account";
    }
}
