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

    public EyeInfo cam_info(String camName) {
        boolean exists = eyes.containsKey(camName);
        EyeInfo.Builder eyeInfo = EyeInfo.newBuilder().setExists(exists);
        if (exists)
            eyeInfo.setCoordinates(eyes.get(camName));
        return eyeInfo.build();
    }

    public ReportResponse report(List<ObjectData> data, String camName) throws RuntimeException{
        System.out.println(camName);
        System.out.println(eyes.get(camName));
        if (!eyes.containsKey(camName)){
            return ReportResponse.newBuilder().setStatus(ReportStatus.UNREGISTERED_EYE).build();
        }
        try {
            for (ObjectData object : data){
                switch (object.getType()) {
                    case PERSON:
                        observations.add(new PersonObservation(Long.parseLong(object.getId()), camName));
                        break;
                    case CAR:
                        if (object.getId().matches("[0-9]{2}[A-Z]{4}") ||
                            object.getId().matches("[A-Z]{2}[0-9]{2}[A-Z]{2}") ||
                            object.getId().matches("[A-Z]{4}[0-9]{2}")){
                            observations.add(new CarObservation(object.getId(), camName));
                            break;
                        } else {
                            return ReportResponse.newBuilder().setStatus(ReportStatus.INVALID_ID).build();
                        }
                    default:
                        throw new RuntimeException("Invalid type");
                }
            }
        } catch (NumberFormatException e){
            return ReportResponse.newBuilder().setStatus(ReportStatus.INVALID_ID).build();
        }

        return ReportResponse.newBuilder().setStatus(ReportStatus.REPORT_OK).build();
    }

    public Observation track(String id, ObjectType type) throws ObservationNotFoundException{
        return observations.stream()
            .filter(o -> o.getType().equals(type))
            .filter(o -> o.getStrId().equals(id))
            .max(Comparator.comparing(Observation::getDate))
            .orElseThrow(() -> new ObservationNotFoundException(id));
    }

    public List<Observation> trackMatch(String id, ObjectType type)/* throws NoObservationMatchExcpetion */{
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

        //if (res.isEmpty()) throw new NoObservationMatchExcpetion(id);

        return res;
    }

    public String ping(String message){
        return "Hello " + message + " !";
    }

    public String clear() {
        observations.clear();
        eyes.clear();
        return "Server has been cleared.";
    }

    public String init(){
        String camName1 = "Tagus";
        Coordinates coordinates1 = Coordinates.newBuilder().setLatitude(38.737613).setLongitude(-9.303164).build();
        String camName2 = "Alameda";
        Coordinates coordinates2 = Coordinates.newBuilder().setLatitude(38.736748).setLongitude(-9.138908).build();

        eyes.put(camName1, coordinates1);
        eyes.put(camName2, coordinates2);

        observations.add(new CarObservation("AA00BB", camName1));
        observations.add(new CarObservation("LD04BY", camName2));
        observations.add(new PersonObservation(123456, camName1));
        observations.add(new CarObservation("4502GS", camName1));
        observations.add(new PersonObservation(654321, camName2));
        observations.add(new CarObservation("AA43BY", camName2));
        observations.add(new PersonObservation(2568628, camName1));
        observations.add(new PersonObservation(12344321, camName2));

        return "Observations added.";
    }
}
