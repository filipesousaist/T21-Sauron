package pt.tecnico.sauron.silo.domain.exception;

public class NoObservvationFoundException extends Exception{
    public NoObservvationFoundException(String id){
        super("No Observations were found whose id matches the expression: "+id+".");
    }

}
