package bankingsystem.facade;

// Author: Baimakhan Aibek (Facade)

import bankingsystem.builder.director.LoanAgreementDirector;
import bankingsystem.builder.product.LoanAgreement;
import bankingsystem.decorator.concrete.OverdraftProtectionDecorator;
import bankingsystem.decorator.concrete.PerTransactionLimitDecorator;
import bankingsystem.decorator.concrete.SmsAlertsDecorator;
import bankingsystem.facade.subsystem.AccountRegistry;
import bankingsystem.facade.subsystem.BankClock;
import bankingsystem.facade.subsystem.Customer;
import bankingsystem.facade.subsystem.LoanService;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.factorymethod.creator.AccountFactory;
import bankingsystem.factorymethod.creator.CheckingAccountFactory;
import bankingsystem.factorymethod.creator.PremiumSavingsAccountFactory;
import bankingsystem.factorymethod.creator.SavingsAccountFactory;
import bankingsystem.factorymethod.product.Account;
import bankingsystem.observer.event.FraudAlert;
import bankingsystem.observer.event.Transaction;
import bankingsystem.observer.event.TransactionType;
import bankingsystem.observer.subject.TransactionMonitor;
import bankingsystem.observer.subscriber.AuditLogObserver;
import bankingsystem.observer.subscriber.FraudDetectionObserver;
import bankingsystem.observer.subscriber.TransactionObserver;
import bankingsystem.observer.subscriber.TransactionStatisticsObserver;
import bankingsystem.strategy.InterestStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Facade: one simple entry point to the whole banking subsystem.
 * The client never touches factories, the registry, decorators, the loan service
 * or the transaction monitor directly.
 */
public class BankingFacade {

    private final AccountRegistry registry = new AccountRegistry();
    private final AccountFactory savingsFactory = new SavingsAccountFactory();
    private final AccountFactory premiumSavingsFactory = new PremiumSavingsAccountFactory();
    private final AccountFactory checkingFactory = new CheckingAccountFactory();
    private final LoanAgreementDirector loanDirector = new LoanAgreementDirector();
    private final LoanService loanService = new LoanService();
    private final TransactionMonitor monitor = new TransactionMonitor();
    private final FraudDetectionObserver fraudDetector;
    private final AuditLogObserver auditLog = new AuditLogObserver();
    private final TransactionStatisticsObserver statistics = new TransactionStatisticsObserver();
    private final List<LoanAgreement> approvedLoans = new ArrayList<>();
    private final BankClock clock;
    private final Consumer<String> output;
    private int transactionSequence;

    public BankingFacade(BankClock clock, Consumer<String> output) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.output = Objects.requireNonNull(output, "output");
        this.fraudDetector = new FraudDetectionObserver(alert -> output.accept("FRAUD ALERT: " + alert));
        monitor.subscribe(fraudDetector);
        monitor.subscribe(auditLog);
        monitor.subscribe(statistics);
    }

    // ----- opening accounts (Factory Method) -----

    public String openSavingsAccount(Customer owner, Money initialDeposit) {
        return open(savingsFactory, owner, initialDeposit);
    }

    public String openPremiumSavingsAccount(Customer owner, Money initialDeposit) {
        return open(premiumSavingsFactory, owner, initialDeposit);
    }

    public String openCheckingAccount(Customer owner, Money initialDeposit) {
        return open(checkingFactory, owner, initialDeposit);
    }

    // ----- account features (Decorator) -----

    public void enableOverdraftProtection(String accountId, Money limit) {
        decorate(accountId, new OverdraftProtectionDecorator(registry.find(accountId), limit));
    }

    public void enableSmsAlerts(String accountId) {
        decorate(accountId, new SmsAlertsDecorator(registry.find(accountId), output));
    }

    public void setPerTransactionLimit(String accountId, Money limit) {
        decorate(accountId, new PerTransactionLimitDecorator(registry.find(accountId), limit));
    }

    // ----- money movement (publishes to Observers) -----

    public void deposit(String accountId, Money amount) {
        creditAccount(accountId, amount, TransactionType.DEPOSIT);
    }

    public void withdraw(String accountId, Money amount) {
        registry.find(accountId).withdraw(amount);
        publish(accountId, TransactionType.WITHDRAWAL, amount);
    }

    public void transfer(String fromAccountId, String toAccountId, Money amount) {
        Account source = registry.find(fromAccountId);
        Account target = registry.find(toAccountId);
        source.withdraw(amount);
        publish(fromAccountId, TransactionType.TRANSFER_OUT, amount);
        target.deposit(amount);
        publish(toAccountId, TransactionType.TRANSFER_IN, amount);
    }

    // ----- interest (Strategy) -----

    public Money projectInterest(String accountId, int months) {
        return registry.find(accountId).calculateInterest(months);
    }

    public void switchInterestStrategy(String accountId, InterestStrategy strategy) {
        registry.find(accountId).setInterestStrategy(strategy);
    }

    public String describeInterest(String accountId) {
        return registry.find(accountId).describeInterest();
    }

    // ----- loans (Builder + Director) -----

    public LoanAgreement applyForPersonalLoan(Customer borrower, String payoutAccountId,
                                              Money principal, int termMonths) {
        return submitLoan(loanDirector.personalLoan(borrower, principal, termMonths), payoutAccountId);
    }

    public LoanAgreement applyForMortgage(Customer borrower, String payoutAccountId,
                                          Money principal, int years, String property) {
        return submitLoan(loanDirector.mortgage(borrower, principal, years, property), payoutAccountId);
    }

    /** Accepts an agreement built by the client with LoanAgreement.builder(...). */
    public LoanAgreement submitLoan(LoanAgreement agreement, String payoutAccountId) {
        loanService.ensureEligible(agreement);
        creditAccount(payoutAccountId, agreement.getPrincipal(), TransactionType.LOAN_DISBURSEMENT);
        approvedLoans.add(agreement);
        return agreement;
    }

    // ----- reading state -----

    public Money getBalance(String accountId) {
        return registry.find(accountId).getBalance();
    }

    public String describeAccount(String accountId) {
        Account account = registry.find(accountId);
        return String.format("%s | %s | %s | balance %s | monthly fee %s",
                account.getId(), account.getOwner().name(), account.describe(),
                account.getBalance(), account.getMonthlyFee());
    }

    public List<String> describeAllAccounts() {
        List<String> lines = new ArrayList<>();
        for (Account account : registry.all()) {
            lines.add(describeAccount(account.getId()));
        }
        return lines;
    }

    public List<FraudAlert> getFraudAlerts() {
        return fraudDetector.getAlerts();
    }

    public List<String> getAuditLog() {
        return auditLog.getEntries();
    }

    public String getStatisticsSummary() {
        return statistics.summary();
    }

    public List<LoanAgreement> getApprovedLoans() {
        return Collections.unmodifiableList(approvedLoans);
    }

    public void subscribe(TransactionObserver observer) {
        monitor.subscribe(observer);
    }

    // ----- private helpers -----

    private String open(AccountFactory factory, Customer owner, Money initialDeposit) {
        Account account = factory.openAccount(owner, initialDeposit);
        registry.register(account);
        if (initialDeposit.isPositive()) {
            publish(account.getId(), TransactionType.DEPOSIT, initialDeposit);
        }
        return account.getId();
    }

    private void decorate(String accountId, Account decorated) {
        registry.replace(accountId, decorated);
    }

    private void creditAccount(String accountId, Money amount, TransactionType type) {
        registry.find(accountId).deposit(amount);
        publish(accountId, type, amount);
    }

    private void publish(String accountId, TransactionType type, Money amount) {
        transactionSequence++;
        String id = String.format("TX-%04d", transactionSequence);
        monitor.publish(new Transaction(id, accountId, type, amount, clock.now()));
    }
}
