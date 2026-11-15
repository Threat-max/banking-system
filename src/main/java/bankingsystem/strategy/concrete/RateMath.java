package bankingsystem.strategy.concrete;

// Author: Urazbek Bek (Strategy)

import java.math.BigDecimal;
import java.math.MathContext;

/** Shared rate arithmetic for the strategies of this package. */
final class RateMath {

    static final MathContext PRECISION = MathContext.DECIMAL128;
    static final int MONTHS_PER_YEAR = 12;

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    private RateMath() {
    }

    static BigDecimal percentToFraction(BigDecimal percent) {
        return percent.divide(PERCENT, PRECISION);
    }

    static BigDecimal monthlyRate(BigDecimal annualPercent) {
        return percentToFraction(annualPercent)
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), PRECISION);
    }

    static void requireNonNegativeMonths(int months) {
        if (months < 0) {
            throw new IllegalArgumentException("Months must not be negative: " + months);
        }
    }
}
