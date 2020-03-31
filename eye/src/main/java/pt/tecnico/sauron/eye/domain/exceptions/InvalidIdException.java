package pt.tecnico.sauron.eye.domain.exceptions;

public class InvalidIdException extends Exception {
    @Override
    public String getMessage() {
        return "One or more of the IDs of the reported objects were invalid.";
    }
}
