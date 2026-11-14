package bankingsystem.facade.exception;

public class LoanRejectedException extends BankingException {

    private static final long serialVersionUID = 1L;

    public LoanRejectedException(String message) {
        super(message);
    }
}
