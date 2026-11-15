package bankingsystem.facade.subsystem;

import java.util.Objects;

/** A bank customer. Credit score and income are used by the loan rules. */
public record Customer(String id, String name, int creditScore, Money monthlyIncome) {

    private static final int MIN_CREDIT_SCORE = 300;
    private static final int MAX_CREDIT_SCORE = 850;

    public Customer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(monthlyIncome, "monthlyIncome");
        if (creditScore < MIN_CREDIT_SCORE || creditScore > MAX_CREDIT_SCORE) {
            throw new IllegalArgumentException("Credit score must be between "
                    + MIN_CREDIT_SCORE + " and " + MAX_CREDIT_SCORE + ": " + creditScore);
        }
    }
}
