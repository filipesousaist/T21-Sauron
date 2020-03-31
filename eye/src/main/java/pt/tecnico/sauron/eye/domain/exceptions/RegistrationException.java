package pt.tecnico.sauron.eye.domain.exceptions;

public class RegistrationException extends Exception {
    private String message;

    public RegistrationException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
