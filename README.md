# Banking System: Final Project (Software Design Patterns)

A Java console application that integrates **six design patterns** in one cohesive project:
Factory Method, Builder, Strategy, Decorator, Observer and Facade.
No external libraries. Requires JDK 11 or newer (developed on JDK 21).

**Team:** Tolendi Arsen, Urazbek Bek, Baimakhan Aibek

## Patterns and where they live

| Pattern | Role in the bank | Main classes | Implemented by |
|---|---|---|---|
| Factory Method | Open the right account type | `AccountFactory`, `SavingsAccountFactory`, `PremiumSavingsAccountFactory`, `CheckingAccountFactory` | Tolendi Arsen |
| Builder | Assemble and validate loan agreements | `LoanAgreement.Builder`, `LoanAgreementDirector` | Tolendi Arsen |
| Strategy | Interchangeable interest algorithms | `InterestStrategy` + 4 strategies, context `BankAccount` | Urazbek Bek |
| Decorator | Add overdraft, limit, SMS alerts at runtime | `AccountDecorator` + 3 decorators | Urazbek Bek |
| Observer | Fraud alerts, audit log, dashboard | `TransactionMonitor`, `TransactionObserver` + 3 observers | Baimakhan Aibek |
| Facade | One entry point to the whole subsystem | `BankingFacade` | Baimakhan Aibek |


## How the patterns cooperate

- `BankingFacade` uses the factories to open accounts, decorators to add features, the director/builder for loans and the monitor to publish every transaction.
- `PremiumSavingsAccountFactory` injects `PremiumTieredInterestStrategy`; `CheckingAccountFactory` uses `NoInterestStrategy` (Factory + Strategy).
- `LoanAgreement` computes interest with `AmortizedLoanInterestStrategy` (Builder + Strategy).
- Decorators forward interest calls to the wrapped account (Decorator + Strategy).
- Loan payouts and every money movement become `Transaction` events for the observers (Facade + Observer).

## Project structure

```
src/main/java/bankingsystem/
  Main.java                    the only class outside the pattern packages: demo + 41 self-checks
  factorymethod/               Factory Method (creational)
    creator/                     AccountFactory, SavingsAccountFactory, PremiumSavingsAccountFactory, CheckingAccountFactory
    product/                     Account, BankAccount, SavingsAccount, PremiumSavingsAccount, CheckingAccount
  builder/                     Builder (creational)
    product/                     LoanAgreement (+ inner static Builder)
    director/                    LoanAgreementDirector
  decorator/                   Decorator (structural)
    base/                        AccountDecorator
    concrete/                    OverdraftProtectionDecorator, PerTransactionLimitDecorator, SmsAlertsDecorator
  facade/                      Facade (structural): BankingFacade
    subsystem/                   AccountRegistry, BankClock, LoanService, Money, Customer
    exception/                   BankingException hierarchy (unchecked)
  strategy/                    Strategy (behavioral): InterestStrategy
    concrete/                    Compound, PremiumTiered, AmortizedLoan, NoInterest strategies, RateMath
  observer/                    Observer (behavioral)
    subject/                     TransactionMonitor
    subscriber/                  TransactionObserver, FraudDetectionObserver, AuditLogObserver, TransactionStatisticsObserver
    event/                       Transaction, TransactionType, FraudAlert
docs/
  Banking_System_Report.pdf    report for Moodle (intro, body, conclusion, further work, code, screenshots, UML)
  uml/                         Mermaid sources (.mmd) and PNG diagrams
  screenshots/                 console screenshots of the real program output
  console-output.txt           full output of Main (demo + self-checks)
```

## Build and run

```
mkdir out
javac -d out $(find src -name "*.java")
java -cp out bankingsystem.Main
```

Windows PowerShell:

```
mkdir out
javac -d out (Get-ChildItem -Recurse -Filter *.java src | % FullName)
java -cp out bankingsystem.Main
```

In IntelliJ IDEA: open the folder, mark `src/main/java` as the source root, run `Main`.

## Demo scenario (output of `Main`, see `docs/console-output.txt`)

1. Open savings, premium savings and checking accounts (Factory Method).
2. Add overdraft protection, SMS alerts and a per-transaction limit (Decorator).
3. Overdraft, repayment, rejected withdrawals, transfer (Facade).
4. Fraud alerts: a $6,000 withdrawal and three withdrawals within 10 minutes (Observer).
5. Interest projection with premium, compound and no-interest strategies swapped at runtime (Strategy).
6. Personal loan by the director, a rejected loan without security, a custom loan with a co-signer, a rejected mortgage (Builder).
7. Audit log, dashboard statistics, final accounts.
8. Self-checks of all six patterns: 41 checks, the program exits with code 1 if any fails.

Verified by hand and by an independent calculation: $10,000 at 12% for 24 months gives a $470.73 payment and $1,297.63 interest; $14,000 on the premium tiers gives $500.00 per year; with 4% compounding $570.38.

## Clean Code principles applied

Meaningful names, small single-purpose classes and methods (SRP), no magic numbers (named constants), immutability (`record`, `final`, `BigDecimal` money),
exceptions with context instead of error codes, fail-fast validation and no nulls (`Optional`, null-object strategy), composition over inheritance,
dependency on abstractions (Open/Closed, Dependency Inversion), one entry point (Law of Demeter), and no `instanceof` or type `switch` anywhere.
Details are in the report, section 2.9.
