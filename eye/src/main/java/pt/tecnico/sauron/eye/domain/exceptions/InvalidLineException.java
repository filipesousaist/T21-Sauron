package pt.tecnico.sauron.eye.domain.exceptions;

public class InvalidLineException extends Exception {
    private String message;

    public InvalidLineException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
