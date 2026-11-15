package bankingsystem.strategy;

// Author: Urazbek Bek (Strategy)

import bankingsystem.facade.subsystem.Money;

/**
 * Strategy interface: one family of interest algorithms behind one method.
 * Interest is returned for the given number of months.
 */
public interface InterestStrategy {

    Money calculateInterest(Money principal, int months);

    String describe();
}
