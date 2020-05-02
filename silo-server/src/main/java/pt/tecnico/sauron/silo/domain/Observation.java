package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.time.LocalDate;
import java.util.Date;

public abstract class Observation {

    private Date date;
    private ObjectType type;
    private String camName;

    public Observation(ObjectType type, String camName, Date date) {
        this.date = date;
        this.type = type;
        this.camName = camName;
    }

    public abstract String getStrId();

    public abstract String getIdFormat();

    public Date getDate(){return date;}

    public ObjectType getType(){return type;}

    public String getCamName(){return camName;}

    @Override
    public String toString() {
        return ""+type.toString()+" "+getStrId()+" "+camName;
    }
}