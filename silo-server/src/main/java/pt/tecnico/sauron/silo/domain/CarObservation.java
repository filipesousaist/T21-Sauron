package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;

public class CarObservation extends Observation {

    private String id;

    CarObservation(String id, String camName){
        super(ObjectType.CAR, camName);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getStrId() {
        return id;
    }
}
