package bankingsystem.facade.subsystem;

// Author: Baimakhan Aibek (Facade)

import bankingsystem.facade.exception.AccountNotFoundException;
import bankingsystem.factorymethod.product.Account;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Subsystem class: in-memory storage of accounts by id. Decorating replaces the stored object. */
public class AccountRegistry {

    private final Map<String, Account> accountsById = new LinkedHashMap<>();

    public void register(Account account) {
        accountsById.put(account.getId(), account);
    }

    public Account find(String accountId) {
        Account account = accountsById.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException("No account with id " + accountId);
        }
        return account;
    }

    public void replace(String accountId, Account decorated) {
        find(accountId);
        accountsById.put(accountId, decorated);
    }

    public List<Account> all() {
        return new ArrayList<>(accountsById.values());
    }
}
