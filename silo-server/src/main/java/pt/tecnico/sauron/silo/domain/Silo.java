package pt.tecnico.sauron.silo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.tecnico.sauron.silo.grpc.Silo.*;

public class Silo {

    private Map personObservations = new HashMap<Long, PersonObservation>();
    private Map carObservation = new HashMap<String, CarObservation>();

    public Silo(){

    }

    public Observation track(String id, ObjType type){
       if(type == ObjType.PERSON){

       }
    }
}
