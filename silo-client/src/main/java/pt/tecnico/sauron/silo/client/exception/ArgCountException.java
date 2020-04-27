package pt.tecnico.sauron.silo.client.exception;

public class ArgCountException extends Exception {
    private int argCount;
    private int minArgCount;
    private int maxArgCount;

    public ArgCountException(int argCount, int minArgCount, int maxArgCount) {
        this.argCount = argCount;
        this.minArgCount = minArgCount;
        this.maxArgCount = maxArgCount;
    }

    @Override
    public String getMessage() {
        return "Wrong number of arguments. Expected between " +
                minArgCount + " and " + maxArgCount + ", but got " + argCount + ".";
    }
}
