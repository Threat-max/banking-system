package bankingsystem.facade.exception;

public class TransactionLimitExceededException extends BankingException {

    private static final long serialVersionUID = 1L;

    public TransactionLimitExceededException(String message) {
        super(message);
    }
}
