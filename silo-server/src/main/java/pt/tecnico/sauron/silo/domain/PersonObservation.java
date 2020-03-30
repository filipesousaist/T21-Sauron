package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;

public class PersonObservation extends Observation{

    private long id;

    public PersonObservation(long id){
        super(ObjectType.PERSON);
        this.id = id;
    }

    public long getId(){
        return id;
    }

    @Override
    public String getStrId() {
        return ""+id;
    }
}
