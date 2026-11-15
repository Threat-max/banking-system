package bankingsystem.strategy.concrete;

// Author: Urazbek Bek (Strategy)

import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;

import java.math.BigDecimal;

/** Savings interest, compounded monthly: balance * ((1 + r/12)^months - 1). */
public class CompoundSavingsInterestStrategy implements InterestStrategy {

    private final BigDecimal annualRatePercent;
    private final BigDecimal monthlyRate;

    public CompoundSavingsInterestStrategy(BigDecimal annualRatePercent) {
        this.annualRatePercent = annualRatePercent;
        this.monthlyRate = RateMath.monthlyRate(annualRatePercent);
    }

    @Override
    public Money calculateInterest(Money principal, int months) {
        RateMath.requireNonNegativeMonths(months);
        if (!principal.isPositive()) {
            return Money.ZERO;
        }
        BigDecimal growth = BigDecimal.ONE.add(monthlyRate).pow(months, RateMath.PRECISION);
        return principal.times(growth.subtract(BigDecimal.ONE));
    }

    @Override
    public String describe() {
        return "Compound savings " + annualRatePercent.stripTrailingZeros().toPlainString()
                + "% (monthly compounding)";
    }
}
