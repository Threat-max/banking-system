package bankingsystem.builder.director;

// Author: Tolendi Arsen (Builder)

import bankingsystem.builder.product.LoanAgreement;
import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;

/** Director: knows the standard recipes (rates, terms) for the bank's loan products. */
public class LoanAgreementDirector {

    private static final String PERSONAL_RATE_PERCENT = "12.0";
    private static final String CAR_RATE_PERCENT = "9.0";
    private static final String MORTGAGE_RATE_PERCENT = "6.5";
    private static final int MONTHS_PER_YEAR = 12;

    public LoanAgreement personalLoan(Customer borrower, Money principal, int termMonths) {
        return LoanAgreement.builder(borrower)
                .principal(principal)
                .termMonths(termMonths)
                .annualRatePercent(PERSONAL_RATE_PERCENT)
                .purpose("Personal")
                .build();
    }

    public LoanAgreement carLoan(Customer borrower, Money principal, int termMonths,
                                 String carDescription) {
        return LoanAgreement.builder(borrower)
                .principal(principal)
                .termMonths(termMonths)
                .annualRatePercent(CAR_RATE_PERCENT)
                .purpose("Car")
                .collateral(carDescription)
                .insured(true)
                .build();
    }

    public LoanAgreement mortgage(Customer borrower, Money principal, int years,
                                  String propertyDescription) {
        return LoanAgreement.builder(borrower)
                .principal(principal)
                .termMonths(years * MONTHS_PER_YEAR)
                .annualRatePercent(MORTGAGE_RATE_PERCENT)
                .purpose("Mortgage")
                .collateral(propertyDescription)
                .insured(true)
                .build();
    }
}
