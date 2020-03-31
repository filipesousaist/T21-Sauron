package pt.tecnico.sauron.eye.domain.exceptions;

public class UnregisteredEyeException extends Exception {
    @Override
    public String getMessage() {
        return "Error: Eye is unregistered in Silo server.";
    }
}
