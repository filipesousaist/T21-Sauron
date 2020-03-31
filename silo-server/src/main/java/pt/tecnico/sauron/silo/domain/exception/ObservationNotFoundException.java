package pt.tecnico.sauron.silo.domain.exception;

public class ObservationNotFoundException extends Exception{

    public ObservationNotFoundException(String id){
        super("The observation with id:"+id+" was not found!");
    }
}
