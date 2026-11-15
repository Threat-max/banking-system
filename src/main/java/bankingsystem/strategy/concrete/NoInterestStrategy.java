package bankingsystem.strategy.concrete;

// Author: Urazbek Bek (Strategy)

import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;

/** Null-object strategy for accounts that do not earn interest (for example checking). */
public class NoInterestStrategy implements InterestStrategy {

    @Override
    public Money calculateInterest(Money principal, int months) {
        RateMath.requireNonNegativeMonths(months);
        return Money.ZERO;
    }

    @Override
    public String describe() {
        return "No interest";
    }
}
