package bankingsystem.facade.subsystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/** Immutable money value. BigDecimal with two decimals, so no floating point errors. */
public record Money(BigDecimal amount) implements Comparable<Money> {

    private static final int SCALE = 2;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        Objects.requireNonNull(amount, "amount");
        amount = amount.setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public static Money of(long wholeUnits) {
        return new Money(BigDecimal.valueOf(wholeUnits));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money times(BigDecimal factor) {
        return new Money(amount.multiply(factor));
    }

    public Money dividedBy(int divisor) {
        return new Money(amount.divide(BigDecimal.valueOf(divisor), RoundingMode.HALF_EVEN));
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    public Money min(Money other) {
        return isLessThan(other) ? this : other;
    }

    /** Fails fast when an amount must be strictly positive. */
    public Money requirePositive(String description) {
        if (!isPositive()) {
            throw new IllegalArgumentException(description + " must be positive: " + this);
        }
        return this;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        String sign = amount.signum() < 0 ? "-" : "";
        return String.format(Locale.US, "%s$%,.2f", sign, amount.abs());
    }
}
