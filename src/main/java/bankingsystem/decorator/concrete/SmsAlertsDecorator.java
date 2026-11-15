package bankingsystem.decorator.concrete;

// Author: Urazbek Bek (Decorator)

import bankingsystem.decorator.base.AccountDecorator;
import bankingsystem.facade.subsystem.Money;
import bankingsystem.factorymethod.product.Account;

import java.util.Objects;
import java.util.function.Consumer;

/** Sends a short message to the owner after every successful deposit or withdrawal. */
public class SmsAlertsDecorator extends AccountDecorator {

    private static final Money MONTHLY_FEE = Money.of("1.50");

    private final Consumer<String> smsGateway;

    public SmsAlertsDecorator(Account wrapped, Consumer<String> smsGateway) {
        super(wrapped);
        this.smsGateway = Objects.requireNonNull(smsGateway, "smsGateway");
    }

    @Override
    public void deposit(Money amount) {
        wrapped.deposit(amount);
        send("+" + amount);
    }

    @Override
    public void withdraw(Money amount) {
        wrapped.withdraw(amount);
        send("-" + amount);
    }

    @Override
    public Money getMonthlyFee() {
        return wrapped.getMonthlyFee().plus(MONTHLY_FEE);
    }

    @Override
    public String describe() {
        return wrapped.describe() + " + SMS alerts";
    }

    private void send(String change) {
        smsGateway.accept("[SMS to " + getOwner().name() + "] " + getId() + " " + change
                + ", balance " + getBalance());
    }
}
