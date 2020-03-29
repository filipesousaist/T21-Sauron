package pt.tecnico.sauron.silo.domain;

import java.util.HashMap;
import java.util.Map;

import pt.tecnico.sauron.silo.grpc.Silo.*;

public class Silo {

    private Map observations = new HashMap<Long, Observation>();

    public Silo(){

    }

    public Observation track(String id, ObjType type){
       if(type == ObjType.PERSON){

       }
    }
}
