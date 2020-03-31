package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;

public abstract class Observation {

    private Date date;
    private ObjectType type;

    public Observation(Date date, ObjectType type){
        this.date = date;
        this.type = type;
    }

    public abstract String getId();

    public Date getDate(){return date;}

    public ObjectType getType(){return type;}

}
