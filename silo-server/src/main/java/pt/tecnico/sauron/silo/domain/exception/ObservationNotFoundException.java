package pt.tecnico.sauron.silo.domain.exception;

public class ObservationNotFoundException extends Exception{
    public ObservationNotFoundException(String id){
        super("No Observation was found with id: "+id+".");
    }

}
