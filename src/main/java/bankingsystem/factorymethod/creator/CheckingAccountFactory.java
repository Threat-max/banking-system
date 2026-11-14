package bankingsystem.factorymethod.creator;

// Author: Tolendi Arsen (Factory Method)

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.factorymethod.product.Account;
import bankingsystem.factorymethod.product.CheckingAccount;

public class CheckingAccountFactory extends AccountFactory {

    @Override
    protected Account createAccount(String id, Customer owner) {
        return new CheckingAccount(id, owner);
    }

    @Override
    protected String idPrefix() {
        return "CHK";
    }
}
