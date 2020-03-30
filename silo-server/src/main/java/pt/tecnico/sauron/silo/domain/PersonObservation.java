package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;

public class PersonObservation extends Observation{

    private long id;

    public PersonObservation(long id, Date date){
        super(date, ObjectType.PERSON);
        this.id = id;
    }

    @Override
    public String getId(){
        return ""+id;
    }


}
