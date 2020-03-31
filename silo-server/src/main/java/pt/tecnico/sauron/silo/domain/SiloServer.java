package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import pt.tecnico.sauron.silo.domain.exception.NoObservationMatchExcpetion;
import pt.tecnico.sauron.silo.domain.exception.ObservationNotFoundException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

public class SiloServer {

    private List<Observation> observations = new LinkedList<>();

    public SiloServer(){

    }

    public Observation track(String id, ObjectType type) throws ObservationNotFoundException{
        return observations.stream()
            .filter(o -> o.getType() == type)
            .filter(o -> o.getStrId().equals(id))
            .max(Comparator.comparing(Observation::getDate))
            .orElseThrow(() -> new ObservationNotFoundException(id));
    }

    public List<Observation> trackMatch(String id, ObjectType type) throws NoObservationMatchExcpetion {
        String regex;
        switch (type){
            case PERSON:
                regex = "[0-9]*"; break;
            case CAR:
                regex = "[0-9A-Z]*"; break;
            default:
                regex = ".";
        }

        String pattern = id.replace("*", regex);


       List<Observation> res =  observations.stream()
               .filter(o -> o.getType() == type)
               .filter(o -> o.getStrId().matches(pattern))
               .collect(Collectors.toList());

       if(res.isEmpty()) throw new NoObservationMatchExcpetion(id);

       return res;

    }

    public String clear(){
        observations.clear();
        return "Server has been cleared.";
    }
}
