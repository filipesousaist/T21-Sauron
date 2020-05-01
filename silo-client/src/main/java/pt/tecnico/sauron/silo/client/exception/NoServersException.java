package pt.tecnico.sauron.silo.client.exception;

public class NoServersException extends Exception {
    @Override
    public String getMessage() {
        return "No servers found.";
    }
}
