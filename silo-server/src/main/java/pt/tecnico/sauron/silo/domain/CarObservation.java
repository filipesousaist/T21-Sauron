package pt.tecnico.sauron.silo;

import java.util.Date;

public class CarObservation {
    String id;
    Date date;

    public CarObservation(String id, Date date){
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }
}
