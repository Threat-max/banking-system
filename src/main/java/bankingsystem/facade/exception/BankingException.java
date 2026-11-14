package bankingsystem.facade.exception;

/** Base type of all business errors. Unchecked, so callers are not forced into try/catch noise. */
public class BankingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BankingException(String message) {
        super(message);
    }
}
