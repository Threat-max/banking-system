package bankingsystem.strategy.concrete;

// Author: Urazbek Bek (Strategy)

import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;

import java.math.BigDecimal;

/**
 * Loan interest on a reducing balance (annuity). Returns the total interest paid
 * when the principal is repaid in equal monthly installments over the given months.
 */
public class AmortizedLoanInterestStrategy implements InterestStrategy {

    private final BigDecimal annualRatePercent;
    private final BigDecimal monthlyRate;

    public AmortizedLoanInterestStrategy(BigDecimal annualRatePercent) {
        this.annualRatePercent = annualRatePercent;
        this.monthlyRate = RateMath.monthlyRate(annualRatePercent);
    }

    @Override
    public Money calculateInterest(Money principal, int months) {
        RateMath.requireNonNegativeMonths(months);
        if (!principal.isPositive() || months == 0 || monthlyRate.signum() == 0) {
            return Money.ZERO;
        }
        BigDecimal growth = BigDecimal.ONE.add(monthlyRate).pow(months, RateMath.PRECISION);
        BigDecimal installment = principal.amount()
                .multiply(monthlyRate)
                .multiply(growth)
                .divide(growth.subtract(BigDecimal.ONE), RateMath.PRECISION);
        BigDecimal totalPaid = installment.multiply(BigDecimal.valueOf(months));
        return new Money(totalPaid.subtract(principal.amount()));
    }

    @Override
    public String describe() {
        return "Amortized loan " + annualRatePercent.stripTrailingZeros().toPlainString()
                + "% (reducing balance)";
    }
}
