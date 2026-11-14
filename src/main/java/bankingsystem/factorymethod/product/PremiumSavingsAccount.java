package bankingsystem.factorymethod.product;

// Author: Tolendi Arsen (Factory Method)

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.strategy.InterestStrategy;

/** Concrete product: a savings account for premium clients, opened with a tiered interest strategy. */
public class PremiumSavingsAccount extends SavingsAccount {

    public PremiumSavingsAccount(String id, Customer owner, InterestStrategy interestStrategy) {
        super(id, owner, interestStrategy);
    }

    @Override
    public String describe() {
        return "Premium savings account";
    }
}
