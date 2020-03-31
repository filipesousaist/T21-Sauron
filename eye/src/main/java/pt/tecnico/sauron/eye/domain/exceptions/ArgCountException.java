package pt.tecnico.sauron.eye.domain.exceptions;

public class ArgCountException extends Exception {
    private int argCount;
    private int expectedArgCount;

    public ArgCountException(int argCount, int expectedArgCount) {
        this.argCount = argCount;
        this.expectedArgCount = expectedArgCount;
    }

    @Override
    public String getMessage() {
        return "Wrong number of arguments. Expected " + expectedArgCount + ", but got" + argCount + ".";
    }
}
