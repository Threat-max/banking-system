package bankingsystem.facade.subsystem;

import bankingsystem.builder.product.LoanAgreement;
import bankingsystem.facade.exception.LoanRejectedException;

import java.math.BigDecimal;

/** Subsystem class: decides whether a valid agreement is also affordable and safe for the bank. */
public class LoanService {

    private static final int MIN_CREDIT_SCORE = 500;
    private static final BigDecimal MAX_PAYMENT_TO_INCOME_RATIO = new BigDecimal("0.40");

    public void ensureEligible(LoanAgreement agreement) {
        int score = agreement.getBorrower().creditScore();
        if (score < MIN_CREDIT_SCORE) {
            throw new LoanRejectedException("Loan rejected: credit score " + score
                    + " is below the minimum " + MIN_CREDIT_SCORE);
        }
        Money maxPayment = agreement.getBorrower().monthlyIncome().times(MAX_PAYMENT_TO_INCOME_RATIO);
        if (agreement.getMonthlyPayment().isGreaterThan(maxPayment)) {
            throw new LoanRejectedException("Loan rejected: monthly payment "
                    + agreement.getMonthlyPayment() + " is above 40% of income (" + maxPayment + ")");
        }
    }
}
