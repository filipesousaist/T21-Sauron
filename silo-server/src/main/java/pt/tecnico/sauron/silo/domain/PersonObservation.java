package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;

public class PersonObservation extends Observation{

    private long id;
    static private String idFormat = "[0-9]*";

    public PersonObservation(long id, String camName, Date date){
        super(ObjectType.PERSON, camName, date);
        this.id = id;
    }

    public long getId(){
        return id;
    }

    @Override
    public String getStrId() {
        return ""+id;
    }

    @Override
    public String getIdFormat() {
        return idFormat;
    }
}
