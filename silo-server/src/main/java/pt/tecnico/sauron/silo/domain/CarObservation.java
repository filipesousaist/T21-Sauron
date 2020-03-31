package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;

public class CarObservation extends Observation {

    private String id;

    public CarObservation(String id, Date date){
        super(date, ObjectType.CAR);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

}
