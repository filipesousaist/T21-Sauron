package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exception.InvalidIdException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;

public class CarObservation extends Observation {

    private String id;
    static private String idFormat = "[A-Z]{4}[0-9]{2}" +
                                    "|[A-Z]{2}[0-9]{4}"+
                                    "|[A-Z]{2}[0-9]{2}[A-Z]{2}"+
                                    "|[0-9]{4}[A-Z]{2}"+
                                    "|[0-9]{2}[A-Z]{4}"+
                                    "|[0-9]{2}[A-Z]{2}[0-9]{2}";


    CarObservation(String id, String camName, Date date){
        super(ObjectType.CAR, camName, date);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getStrId() {
        return id;
    }

    @Override
    public String getIdFormat() {
        return idFormat;
    }


    public static String getValidatedId(String id) throws InvalidIdException {
        int numNonDigits = id.replaceAll("[0-9]", "").length();
        if (id.matches("([0-9]{2}|[A-Z]{2}){3}") && (numNonDigits == 2 || numNonDigits == 4))
            return id;
        else
            throw new InvalidIdException("Car ID does not match the specification");
    }
}
