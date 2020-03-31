package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.stream.Collectors;

import pt.tecnico.sauron.silo.domain.exception.NoObservationMatchExcpetion;
import pt.tecnico.sauron.silo.domain.exception.ObservationNotFoundException;
import pt.tecnico.sauron.silo.grpc.Silo.*;


public class SiloServer {
    private static final double ADMISSIBLE_ERROR = 0.000000001;
    private List<Observation> observations = new LinkedList<>();
    private Map<String, Coordinates> eyes = new HashMap<>();

    public SiloServer() {

    }

    public EyeJoinStatus cam_join(String cam_name, Coordinates coordinates) {
        // Check cam_name
        if (!cam_name.matches("[0-9A-Za-z]{3,15}"))
            return EyeJoinStatus.INVALID_EYE_NAME;

        // Check coordinates
        double latitude = coordinates.getLatitude();
        double longitude = coordinates.getLongitude();
        if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180)
            return EyeJoinStatus.INVALID_COORDINATES;

        // Try to add Eye to the "database"
        if (eyes.containsKey(cam_name)) {
            Coordinates dbCoords = eyes.get(cam_name);
            if (Math.abs(latitude - dbCoords.getLatitude()) < ADMISSIBLE_ERROR &&
                Math.abs(longitude - dbCoords.getLongitude()) < ADMISSIBLE_ERROR)
                return EyeJoinStatus.DUPLICATE_JOIN;
            else
                return EyeJoinStatus.REPEATED_NAME;
        }
        else {
            eyes.put(cam_name, coordinates);
            return EyeJoinStatus.JOIN_OK;
        }
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
        switch (type) {
            case PERSON:
                regex = "[0-9]*";
                break;
            case CAR:
                regex = "[0-9A-Z]*";
                break;
            default:
                regex = ".";
        }

        String pattern = id.replace("*", regex);

        List<Observation> res = observations.stream()
                .filter(o -> o.getType() == type)
                .filter(o -> o.getStrId().matches(pattern))
                .collect(Collectors.toList());

        if (res.isEmpty()) throw new NoObservationMatchExcpetion(id);

        return res;
    }

        public String ping(String message){
            return "Hello " + message + " !";
        }

    public String clear() {
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
