package bankingsystem;

import bankingsystem.builder.product.LoanAgreement;
import bankingsystem.decorator.concrete.OverdraftProtectionDecorator;
import bankingsystem.facade.BankingFacade;
import bankingsystem.facade.exception.AccountNotFoundException;
import bankingsystem.facade.exception.BankingException;
import bankingsystem.facade.exception.InsufficientFundsException;
import bankingsystem.facade.exception.LoanRejectedException;
import bankingsystem.facade.subsystem.BankClock;
import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.factorymethod.creator.AccountFactory;
import bankingsystem.factorymethod.creator.CheckingAccountFactory;
import bankingsystem.factorymethod.creator.PremiumSavingsAccountFactory;
import bankingsystem.factorymethod.creator.SavingsAccountFactory;
import bankingsystem.factorymethod.product.Account;
import bankingsystem.observer.event.TransactionType;
import bankingsystem.strategy.concrete.AmortizedLoanInterestStrategy;
import bankingsystem.strategy.concrete.CompoundSavingsInterestStrategy;
import bankingsystem.strategy.concrete.NoInterestStrategy;
import bankingsystem.strategy.concrete.PremiumTieredInterestStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The only class outside the pattern packages.
 * Part 1 is a demo scenario that talks to the BankingFacade only.
 * Part 2 checks every pattern (no JUnit needed). Exit code is 1 if any check fails.
 */
public class Main {

    private static final Customer ALICE = new Customer("C-1", "Alice", 720, Money.of(6_000));
    private static final Customer BOB = new Customer("C-2", "Bob", 580, Money.of(2_500));

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        runDemo();
        runChecks();
    }

    // =====================================================================
    // Part 1: demo scenario
    // =====================================================================

    private static void runDemo() {
        BankClock clock = new BankClock(LocalDateTime.of(2026, 11, 11, 9, 0));
        BankingFacade bank = new BankingFacade(clock, System.out::println);

        section("1. Opening accounts (Factory Method)");
        String aliceSavings = bank.openSavingsAccount(ALICE, Money.of(12_000));
        String aliceChecking = bank.openCheckingAccount(ALICE, Money.of(2_000));
        String bobPremium = bank.openPremiumSavingsAccount(BOB, Money.of(20_000));
        String bobChecking = bank.openCheckingAccount(BOB, Money.of(800));
        bank.describeAllAccounts().forEach(System.out::println);

        section("2. Adding features at runtime (Decorator)");
        bank.enableOverdraftProtection(aliceChecking, Money.of(500));
        bank.enableSmsAlerts(aliceChecking);
        bank.setPerTransactionLimit(aliceSavings, Money.of(3_000));
        System.out.println(bank.describeAccount(aliceChecking));
        System.out.println(bank.describeAccount(aliceSavings));

        section("3. Money movement through the facade (Facade)");
        bank.withdraw(aliceChecking, Money.of(2_300));
        System.out.println("Alice checking after overdraft: " + bank.getBalance(aliceChecking));
        bank.deposit(aliceChecking, Money.of(500));
        System.out.println("Alice checking after deposit:   " + bank.getBalance(aliceChecking));
        attempt("withdraw 900 beyond the overdraft limit", () -> bank.withdraw(aliceChecking, Money.of(900)));
        attempt("withdraw 4000 above the per-transaction limit", () -> bank.withdraw(aliceSavings, Money.of(4_000)));
        bank.transfer(aliceSavings, aliceChecking, Money.of(2_500));
        System.out.println("After transfer: savings " + bank.getBalance(aliceSavings)
                + ", checking " + bank.getBalance(aliceChecking));

        section("4. Fraud detection (Observer)");
        clock.advanceMinutes(30);
        bank.withdraw(bobPremium, Money.of(6_000));
        for (int i = 1; i <= 3; i++) {
            clock.advanceMinutes(2);
            bank.withdraw(aliceChecking, Money.of(100));
        }

        section("5. Interest strategies swapped at runtime (Strategy)");
        printProjection(bank, bobPremium);
        bank.switchInterestStrategy(bobPremium, new CompoundSavingsInterestStrategy(new BigDecimal("4.0")));
        printProjection(bank, bobPremium);
        bank.switchInterestStrategy(bobPremium, new PremiumTieredInterestStrategy());
        printProjection(bank, bobPremium);
        printProjection(bank, aliceChecking);

        section("6. Loans (Builder, Director, Facade)");
        LoanAgreement personal = bank.applyForPersonalLoan(ALICE, aliceChecking, Money.of(10_000), 24);
        System.out.println("APPROVED: " + personal.summary());
        attempt("Bob builds a loan without security", () ->
                LoanAgreement.builder(BOB).principal(Money.of(15_000)).termMonths(36)
                        .annualRatePercent("11.5").purpose("Renovation").build());
        LoanAgreement renovation = LoanAgreement.builder(BOB)
                .principal(Money.of(15_000))
                .termMonths(36)
                .annualRatePercent("11.5")
                .purpose("Renovation")
                .coSigner(ALICE)
                .build();
        System.out.println("APPROVED: " + bank.submitLoan(renovation, bobChecking).summary());
        attempt("Bob applies for a 200,000 mortgage", () ->
                bank.applyForMortgage(BOB, bobChecking, Money.of(200_000), 20, "Apartment, Almaty"));

        section("7. Observers: audit log and dashboard");
        bank.getAuditLog().forEach(System.out::println);
        System.out.println();
        System.out.println(bank.getStatisticsSummary());
        System.out.println();
        System.out.println("Fraud alerts raised: " + bank.getFraudAlerts().size());

        section("8. Final accounts");
        bank.describeAllAccounts().forEach(System.out::println);
    }

    private static void printProjection(BankingFacade bank, String accountId) {
        System.out.printf("%s | %-48s | 12-month interest on %s: %s%n",
                accountId, bank.describeInterest(accountId),
                bank.getBalance(accountId), bank.projectInterest(accountId, 12));
    }

    private static void attempt(String description, Runnable action) {
        try {
            action.run();
            System.out.println("Unexpectedly succeeded: " + description);
        } catch (BankingException | IllegalStateException error) {
            System.out.println("REJECTED (" + description + "): " + error.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    // =====================================================================
    // Part 2: checks of every pattern
    // =====================================================================

    private static void runChecks() {
        section("9. Self-checks of all six patterns");
        factoryCreatesDifferentProducts();
        factoryMethodGeneratesSequentialIds();
        strategiesComputeExpectedInterest();
        strategyCanBeSwappedAtRuntime();
        builderBuildsAndValidates();
        decoratorsStackAndKeepBalanceCorrect();
        observersReceiveEveryTransaction();
        fraudRulesFire();
        facadeHidesSubsystemAndRejectsBadLoans();

        System.out.println();
        System.out.println("Passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- Factory Method ----------

    private static void factoryCreatesDifferentProducts() {
        Account savings = new SavingsAccountFactory().openAccount(ALICE, Money.of(100));
        Account premium = new PremiumSavingsAccountFactory().openAccount(ALICE, Money.of(100));
        Account checking = new CheckingAccountFactory().openAccount(ALICE, Money.of(100));
        check("savings product type", savings.describe().equals("Savings account"));
        check("premium savings product type", premium.describe().equals("Premium savings account"));
        check("checking product type", checking.describe().equals("Checking account"));
        check("initial deposit applied", savings.getBalance().equals(Money.of(100)));
    }

    private static void factoryMethodGeneratesSequentialIds() {
        AccountFactory factory = new SavingsAccountFactory();
        String first = factory.openAccount(ALICE, Money.ZERO).getId();
        String second = factory.openAccount(ALICE, Money.ZERO).getId();
        check("first id", first.equals("SAV-1001"));
        check("second id", second.equals("SAV-1002"));
    }

    // ---------- Strategy ----------

    private static void strategiesComputeExpectedInterest() {
        check("compound 4% on 14,000 for 12 months",
                new CompoundSavingsInterestStrategy(new BigDecimal("4.0"))
                        .calculateInterest(Money.of(14_000), 12).equals(Money.of("570.38")));
        check("tiered on 14,000 for 12 months",
                new PremiumTieredInterestStrategy().calculateInterest(Money.of(14_000), 12)
                        .equals(Money.of("500.00")));
        check("loan interest 10,000 at 12% for 24 months",
                new AmortizedLoanInterestStrategy(new BigDecimal("12.0"))
                        .calculateInterest(Money.of(10_000), 24).equals(Money.of("1297.63")));
        check("no interest", new NoInterestStrategy().calculateInterest(Money.of(999), 12).equals(Money.ZERO));
        check("negative balance earns nothing",
                new CompoundSavingsInterestStrategy(new BigDecimal("4.0"))
                        .calculateInterest(Money.of(-10), 12).equals(Money.ZERO));
    }

    private static void strategyCanBeSwappedAtRuntime() {
        Account account = new SavingsAccountFactory().openAccount(ALICE, Money.of(14_000));
        Money compound = account.calculateInterest(12);
        account.setInterestStrategy(new PremiumTieredInterestStrategy());
        Money tiered = account.calculateInterest(12);
        check("same account, different result after swap", !compound.equals(tiered));
        check("tiered result after swap", tiered.equals(Money.of("500.00")));
    }

    // ---------- Builder ----------

    private static void builderBuildsAndValidates() {
        LoanAgreement loan = LoanAgreement.builder(ALICE).principal(Money.of(10_000))
                .termMonths(24).annualRatePercent("12.0").build();
        check("monthly payment 470.73", loan.getMonthlyPayment().equals(Money.of("470.73")));
        check("optional parts default to empty", loan.getCollateral().isEmpty() && loan.getCoSigner().isEmpty());

        check("low score without security is rejected by builder", throwsException(IllegalStateException.class, () ->
                LoanAgreement.builder(BOB).principal(Money.of(1_000)).termMonths(12)
                        .annualRatePercent("10").build()));
        check("large loan without collateral is rejected by builder", throwsException(IllegalStateException.class, () ->
                LoanAgreement.builder(ALICE).principal(Money.of(60_000)).termMonths(60)
                        .annualRatePercent("10").build()));
        check("missing rate is rejected by builder", throwsException(IllegalStateException.class, () ->
                LoanAgreement.builder(ALICE).principal(Money.of(1_000)).termMonths(12).build()));
        check("co-signer satisfies the security rule", !throwsException(IllegalStateException.class, () ->
                LoanAgreement.builder(BOB).principal(Money.of(1_000)).termMonths(12)
                        .annualRatePercent("10").coSigner(ALICE).build()));
    }

    // ---------- Decorator ----------

    private static void decoratorsStackAndKeepBalanceCorrect() {
        Account base = new CheckingAccountFactory().openAccount(ALICE, Money.of(200));
        OverdraftProtectionDecorator account = new OverdraftProtectionDecorator(base, Money.of(500));
        account.withdraw(Money.of(500));
        check("overdraft balance is negative", account.getBalance().equals(Money.of(-300)));
        check("wrapped account is empty, not negative", base.getBalance().equals(Money.ZERO));
        account.deposit(Money.of(400));
        check("deposit repays overdraft first", account.getBalance().equals(Money.of(100)));
        check("limit enforced", throwsException(InsufficientFundsException.class,
                () -> account.withdraw(Money.of(700))));
        check("fee is summed over decorators", account.getMonthlyFee().equals(Money.of("8.00")));
    }

    // ---------- Observer ----------

    private static void observersReceiveEveryTransaction() {
        BankingFacade bank = newQuietBank(new BankClock(LocalDateTime.of(2026, 1, 1, 9, 0)));
        String id = bank.openSavingsAccount(ALICE, Money.of(1_000));
        bank.deposit(id, Money.of(50));
        bank.withdraw(id, Money.of(20));
        check("audit log has one line per transaction", bank.getAuditLog().size() == 3);
        check("statistics count deposits", bank.getStatisticsSummary().contains("DEPOSIT           count=2"));

        List<String> custom = new ArrayList<>();
        bank.subscribe(transaction -> custom.add(transaction.id()));
        bank.deposit(id, Money.of(1));
        check("late subscriber is notified without changing existing code", custom.equals(List.of("TX-0004")));
    }

    private static void fraudRulesFire() {
        BankClock clock = new BankClock(LocalDateTime.of(2026, 1, 1, 9, 0));
        BankingFacade bank = newQuietBank(clock);
        String id = bank.openSavingsAccount(ALICE, Money.of(50_000));
        bank.withdraw(id, Money.of(4_999));
        check("below threshold: no alert", bank.getFraudAlerts().isEmpty());
        bank.withdraw(id, Money.of(5_000));
        check("large transaction raises alert", bank.getFraudAlerts().size() == 1);

        clock.advanceMinutes(60);
        for (int i = 0; i < 3; i++) {
            clock.advanceMinutes(2);
            bank.withdraw(id, Money.of(10));
        }
        check("rapid succession raises alert", bank.getFraudAlerts().size() == 2);

        BankClock slowClock = new BankClock(LocalDateTime.of(2026, 1, 1, 9, 0));
        BankingFacade slowBank = newQuietBank(slowClock);
        String slowId = slowBank.openSavingsAccount(ALICE, Money.of(1_000));
        for (int i = 0; i < 3; i++) {
            slowClock.advanceMinutes(11);
            slowBank.withdraw(slowId, Money.of(10));
        }
        check("slow withdrawals raise no alert", slowBank.getFraudAlerts().isEmpty());
        check("deposits are never suspicious", newQuietBank(clock).getFraudAlerts().isEmpty());
    }

    // ---------- Facade ----------

    private static void facadeHidesSubsystemAndRejectsBadLoans() {
        BankingFacade bank = newQuietBank(new BankClock(LocalDateTime.of(2026, 1, 1, 9, 0)));
        String from = bank.openSavingsAccount(ALICE, Money.of(1_000));
        String to = bank.openCheckingAccount(BOB, Money.of(0));
        bank.transfer(from, to, Money.of(250));
        check("transfer moves money", bank.getBalance(from).equals(Money.of(750))
                && bank.getBalance(to).equals(Money.of(250)));
        check("transfer is audited twice", bank.getAuditLog().size() == 3);
        check("unknown account fails", throwsException(AccountNotFoundException.class,
                () -> bank.getBalance("NOPE-1")));

        bank.applyForPersonalLoan(ALICE, from, Money.of(10_000), 24);
        check("loan disbursed to account", bank.getBalance(from).equals(Money.of(10_750)));
        check("loan stored", bank.getApprovedLoans().size() == 1);
        check("mortgage rejected by affordability rule", throwsException(LoanRejectedException.class, () ->
                bank.applyForMortgage(BOB, to, Money.of(200_000), 20, "Flat")));
        check("rejected loan pays out nothing", bank.getBalance(to).equals(Money.of(250)));
        check("disbursement counted in statistics", bank.getStatisticsSummary()
                .contains("LOAN_DISBURSEMENT count=1"));
        check("outgoing types are flagged", TransactionType.WITHDRAWAL.isOutgoing()
                && !TransactionType.DEPOSIT.isOutgoing());
    }

    // ---------- helpers ----------

    private static BankingFacade newQuietBank(BankClock clock) {
        return new BankingFacade(clock, message -> { });
    }

    private static boolean throwsException(Class<? extends RuntimeException> expected, Runnable action) {
        try {
            action.run();
            return false;
        } catch (RuntimeException error) {
            return expected.isInstance(error);
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }
}
