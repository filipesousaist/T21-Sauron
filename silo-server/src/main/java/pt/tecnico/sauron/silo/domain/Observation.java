package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.time.LocalDate;
import java.util.Date;

public abstract class Observation {

    private Date date;
    private ObjectType type;

    public Observation(ObjectType type){
        this.date = new Date();
        this.type = type;
    }

    public abstract String getStrId();

    public Date getDate(){return date;}

    public ObjectType getType(){return type;}

}
