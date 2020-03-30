package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.stream.Collectors;

import pt.tecnico.sauron.silo.grpc.Silo.*;

public class SiloServer {

    private List observations = new LinkedList<Observation>();

    public SiloServer(){

    }

    /*public Observation track(String id, ObjectType type){
        if(type == ObjectType.PERSON){
            try{
                Integer.parseInt(id);
                return observations.stream().findAny(obs -> obs.getId());
            }catch (NumberFormatException nfe){

            }
        }
    }*/

    public String clear(){
        observations = null;
        return "Server has been cleared.";
    }
}
