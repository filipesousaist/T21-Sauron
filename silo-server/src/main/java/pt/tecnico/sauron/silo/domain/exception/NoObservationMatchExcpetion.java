package pt.tecnico.sauron.silo.domain.exception;

public class NoObservationMatchExcpetion extends Exception {

    public NoObservationMatchExcpetion(String pattern){
        super("No observation was found that matached the pattern: "+pattern);
    }
}
