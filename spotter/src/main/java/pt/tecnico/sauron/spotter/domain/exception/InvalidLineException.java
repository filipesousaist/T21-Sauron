package pt.tecnico.sauron.spotter.domain.exception;

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
