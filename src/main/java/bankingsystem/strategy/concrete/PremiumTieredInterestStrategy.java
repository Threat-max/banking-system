package bankingsystem.strategy.concrete;

// Author: Urazbek Bek (Strategy)

import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;

import java.math.BigDecimal;

/** Premium savings: simple interest, a low rate up to a threshold and a higher rate above it. */
public class PremiumTieredInterestStrategy implements InterestStrategy {

    private static final Money TIER_THRESHOLD = Money.of(10_000);
    private static final BigDecimal BASE_RATE_PERCENT = new BigDecimal("3.0");
    private static final BigDecimal BONUS_RATE_PERCENT = new BigDecimal("5.0");

    @Override
    public Money calculateInterest(Money principal, int months) {
        RateMath.requireNonNegativeMonths(months);
        if (!principal.isPositive()) {
            return Money.ZERO;
        }
        Money basePart = principal.min(TIER_THRESHOLD);
        Money bonusPart = principal.minus(basePart);
        return interestFor(basePart, BASE_RATE_PERCENT, months)
                .plus(interestFor(bonusPart, BONUS_RATE_PERCENT, months));
    }

    private Money interestFor(Money part, BigDecimal annualPercent, int months) {
        BigDecimal yearFraction = BigDecimal.valueOf(months)
                .divide(BigDecimal.valueOf(RateMath.MONTHS_PER_YEAR), RateMath.PRECISION);
        return part.times(RateMath.percentToFraction(annualPercent).multiply(yearFraction));
    }

    @Override
    public String describe() {
        return "Premium tiered: 3% up to " + TIER_THRESHOLD + ", 5% above";
    }
}
