package bankingsystem.factorymethod.creator;

// Author: Tolendi Arsen (Factory Method)

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.factorymethod.product.Account;
import bankingsystem.factorymethod.product.PremiumSavingsAccount;
import bankingsystem.strategy.concrete.PremiumTieredInterestStrategy;

/** Creates the premium savings product, wired with the tiered interest strategy. */
public class PremiumSavingsAccountFactory extends AccountFactory {

    @Override
    protected Account createAccount(String id, Customer owner) {
        return new PremiumSavingsAccount(id, owner, new PremiumTieredInterestStrategy());
    }

    @Override
    protected String idPrefix() {
        return "PRM";
    }
}
