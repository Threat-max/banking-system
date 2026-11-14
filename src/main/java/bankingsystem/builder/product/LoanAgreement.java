package bankingsystem.builder.product;

// Author: Tolendi Arsen (Builder)

import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.strategy.InterestStrategy;
import bankingsystem.strategy.concrete.AmortizedLoanInterestStrategy;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * Product of the Builder pattern: an immutable loan agreement.
 * It has several optional parts (collateral, co-signer, insurance), so it is
 * created only through {@link Builder}, which also validates the combination.
 */
public final class LoanAgreement {

    private final Customer borrower;
    private final Money principal;
    private final int termMonths;
    private final BigDecimal annualRatePercent;
    private final String purpose;
    private final String collateral;
    private final Customer coSigner;
    private final boolean insured;
    private final Money totalInterest;

    private LoanAgreement(Builder builder) {
        this.borrower = builder.borrower;
        this.principal = builder.principal;
        this.termMonths = builder.termMonths;
        this.annualRatePercent = builder.annualRatePercent;
        this.purpose = builder.purpose;
        this.collateral = builder.collateral;
        this.coSigner = builder.coSigner;
        this.insured = builder.insured;
        InterestStrategy strategy = new AmortizedLoanInterestStrategy(annualRatePercent);
        this.totalInterest = strategy.calculateInterest(principal, termMonths);
    }

    public static Builder builder(Customer borrower) {
        return new Builder(borrower);
    }

    public Customer getBorrower() {
        return borrower;
    }

    public Money getPrincipal() {
        return principal;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public BigDecimal getAnnualRatePercent() {
        return annualRatePercent;
    }

    public String getPurpose() {
        return purpose;
    }

    public Optional<String> getCollateral() {
        return Optional.ofNullable(collateral);
    }

    public Optional<Customer> getCoSigner() {
        return Optional.ofNullable(coSigner);
    }

    public boolean isInsured() {
        return insured;
    }

    public Money getTotalInterest() {
        return totalInterest;
    }

    public Money getTotalRepayment() {
        return principal.plus(totalInterest);
    }

    public Money getMonthlyPayment() {
        return getTotalRepayment().dividedBy(termMonths);
    }

    public String summary() {
        return String.format("%s loan for %s: %s over %d months at %s%%, monthly payment %s, "
                        + "total interest %s, collateral: %s, co-signer: %s, insured: %s",
                purpose, borrower.name(), principal, termMonths,
                annualRatePercent.stripTrailingZeros().toPlainString(),
                getMonthlyPayment(), totalInterest,
                getCollateral().orElse("none"),
                getCoSigner().map(Customer::name).orElse("none"),
                insured ? "yes" : "no");
    }

    /** Step-by-step builder with a final validation in build(). */
    public static final class Builder {

        private static final int MIN_TERM_MONTHS = 1;
        private static final int MAX_TERM_MONTHS = 360;
        private static final Money COLLATERAL_REQUIRED_ABOVE = Money.of(50_000);
        private static final int MIN_SCORE_WITHOUT_SECURITY = 600;

        private final Customer borrower;
        private Money principal;
        private int termMonths;
        private BigDecimal annualRatePercent;
        private String purpose = "General purpose";
        private String collateral;
        private Customer coSigner;
        private boolean insured;

        private Builder(Customer borrower) {
            this.borrower = Objects.requireNonNull(borrower, "borrower");
        }

        public Builder principal(Money principal) {
            this.principal = principal;
            return this;
        }

        public Builder termMonths(int termMonths) {
            this.termMonths = termMonths;
            return this;
        }

        public Builder annualRatePercent(String annualRatePercent) {
            this.annualRatePercent = new BigDecimal(annualRatePercent);
            return this;
        }

        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder collateral(String collateral) {
            this.collateral = collateral;
            return this;
        }

        public Builder coSigner(Customer coSigner) {
            this.coSigner = coSigner;
            return this;
        }

        public Builder insured(boolean insured) {
            this.insured = insured;
            return this;
        }

        public LoanAgreement build() {
            validate();
            return new LoanAgreement(this);
        }

        private void validate() {
            if (principal == null || !principal.isPositive()) {
                throw new IllegalStateException("A positive principal is required");
            }
            if (annualRatePercent == null || annualRatePercent.signum() < 0) {
                throw new IllegalStateException("A non-negative annual rate is required");
            }
            if (termMonths < MIN_TERM_MONTHS || termMonths > MAX_TERM_MONTHS) {
                throw new IllegalStateException("Term must be between " + MIN_TERM_MONTHS
                        + " and " + MAX_TERM_MONTHS + " months: " + termMonths);
            }
            if (principal.isGreaterThan(COLLATERAL_REQUIRED_ABOVE) && collateral == null) {
                throw new IllegalStateException("Loans above " + COLLATERAL_REQUIRED_ABOVE
                        + " require collateral");
            }
            boolean hasSecurity = collateral != null || coSigner != null;
            if (borrower.creditScore() < MIN_SCORE_WITHOUT_SECURITY && !hasSecurity) {
                throw new IllegalStateException("Credit score below " + MIN_SCORE_WITHOUT_SECURITY
                        + " requires a co-signer or collateral");
            }
        }
    }
}
