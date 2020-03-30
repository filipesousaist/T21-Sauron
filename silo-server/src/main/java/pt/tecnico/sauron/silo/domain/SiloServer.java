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
            .filter(o -> o.getType() == type)
            .filter(o -> o.getStrId() == id)
            .max(Comparator.comparing(Observation::getDate))
            .orElseThrow(() -> new ObservationNotFoundException(id));
    }

    public String clear(){
        observations.clear();
        return "Server has been cleared.";
    }
}
