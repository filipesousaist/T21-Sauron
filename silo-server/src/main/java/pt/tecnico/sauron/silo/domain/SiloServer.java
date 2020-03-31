package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.stream.Collectors;

import pt.tecnico.sauron.silo.domain.exception.ObservationNotFoundException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

public class SiloServer {

    private List<Observation> observations = new LinkedList<>();

    public SiloServer(){

    }

    public Observation track(String id, ObjectType type) throws ObservationNotFoundException{
        return observations.stream()
            .filter(o -> o.getType().equals(type))
            .filter(o -> o.getStrId().equals(id))
            .max(Comparator.comparing(Observation::getDate))
            .orElseThrow(() -> new ObservationNotFoundException(id));
    }

    public String ping(String message){
        return "Hello " + message + " !";
    }

    public String clear(){
        observations.clear();
        return "Server has been cleared.";
    }

    public String init(){
        observations.add(new CarObservation("AA00BB"));
        observations.add(new CarObservation("LD04BY"));
        observations.add(new PersonObservation(123456));
        observations.add(new CarObservation("4502GS"));
        observations.add(new PersonObservation(654321));
        observations.add(new CarObservation("AA43BY"));
        observations.add(new PersonObservation(2568628));
        observations.add(new PersonObservation(12344321));

        return "Observations added.";
    }
}
