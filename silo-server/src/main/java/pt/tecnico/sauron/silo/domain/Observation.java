package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo;

import java.util.Date;

public abstract class Observation {

    private Date date;

    public String getId();

    public Date getDate();

}
