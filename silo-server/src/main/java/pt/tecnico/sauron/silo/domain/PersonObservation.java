package pt.tecnico.sauron.silo;

import java.util.Date;

public class PersonObservation {

    private long id;
    private Date date;

    public PersonObservation(long id, Date date){
        this.id = id;
        this.date = date;
    }

    public String getId(){
        return ""+id;
    }

    public Date getDate(){ return date;}

}
