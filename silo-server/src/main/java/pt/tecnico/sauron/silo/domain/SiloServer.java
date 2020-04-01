package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.stream.Collectors;

import pt.tecnico.sauron.silo.domain.exception.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;


public class SiloServer {
    private static final double ADMISSIBLE_ERROR = 0.000000001;
    private List<Observation> observations = new LinkedList<>();
    private Map<String, Coordinates> eyes = new HashMap<>();

    public SiloServer() {

    }

    public void cam_join(String cam_name, Coordinates coordinates)
            throws InvalidEyeNameException, InvalidCoordinatesException,
            DuplicateJoinException, RepeatedNameException {
        // Check cam_name
        if (!cam_name.matches("[0-9A-Za-z]{3,15}"))
            throw new InvalidEyeNameException();

        // Check coordinates
        double latitude = coordinates.getLatitude();
        double longitude = coordinates.getLongitude();
        if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180)
            throw new InvalidCoordinatesException();

        // Try to add Eye to the "database"
        if (eyes.containsKey(cam_name)) {
            Coordinates dbCoords = eyes.get(cam_name);
            if (Math.abs(latitude - dbCoords.getLatitude()) < ADMISSIBLE_ERROR &&
                Math.abs(longitude - dbCoords.getLongitude()) < ADMISSIBLE_ERROR)
                throw new DuplicateJoinException();
            else
                throw new RepeatedNameException();
        }
        else
            eyes.put(cam_name, coordinates);
    }

    public Coordinates cam_info(String camName) throws UnregisteredEyeException {
        synchronized (this) {
            if (eyes.containsKey(camName))
                return eyes.get(camName);
        }
        throw new UnregisteredEyeException();
    }

    public void report(List<ObjectData> data, String camName)
            throws InvalidIdException, UnregisteredEyeException {
        if (!eyes.containsKey(camName))
            throw new UnregisteredEyeException();

        Date date = new Date();

        for (ObjectData object : data) {
            switch (object.getType()) {
                case PERSON:
                    try {
                        long personId = Long.parseLong(object.getId());
                        if (personId < 0)
                            throw new InvalidIdException("Person ID does not match the specification");
                        synchronized (this) {
                            observations.add(new PersonObservation(personId, camName, date));
                        }
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidIdException("Person ID does not match the specification");
                    }
                    break;
                case CAR:
                    String carId = object.getId();
                    int numNonDigits = carId.replaceAll("[0-9]", "").length();
                    if (carId.matches("([0-9]{2}|[A-Z]{2}){3}") &&
                        numNonDigits == 2 || numNonDigits == 4) {
                        synchronized (this) {
                            observations.add(new CarObservation(object.getId(), camName, date));
                        }
                        break;
                    }
                    else
                        throw new InvalidIdException("Car ID does not match the specification");
                default:
                    throw new RuntimeException("Invalid type");
            }
        }
    }

    public Optional<Observation> track(String id, ObjectType type) {
        synchronized (this) {
            return observations.stream()
                    .filter(o -> o.getType().equals(type))
                    .filter(o -> o.getStrId().equals(id))
                    .max(Comparator.comparing(Observation::getDate));
        }
    }

    public List<Observation> trackMatch(String id, ObjectType type) {
        String regex;
        switch (type) {
            case PERSON:
                regex = "[0-9]*";
                break;
            case CAR:
                regex = "[0-9A-Z]*";
                break;
            default:
                throw new RuntimeException("Invalid type");
        }

        String pattern = id.replace("*", regex);
        synchronized (this) {
            return observations.stream()
                    .filter(o -> o.getType() == type)
                    .filter(o -> o.getStrId().matches(pattern))
                    .map(Observation::getStrId)
                    .distinct()
                    .map(this::getLatest)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

    }

    private Optional<Observation> getLatest(String id) {
        return observations.stream()
                .filter(obs -> obs.getStrId().equals(id))
                .max(Comparator.comparing(Observation::getDate));
    }

    public List<Observation> trace(String id, ObjectType type) {
        synchronized (this) {
            return observations.stream()
                    .filter(o -> o.getType().equals(type))
                    .filter(o -> o.getStrId().equals(id))
                    .sorted(Comparator.comparing(Observation::getDate))
                    .collect(Collectors.toList());
        }
    }

    public String ping(String message) {
        return "Hello " + message + " !";
    }

    public String clear() {
        observations.clear();
        eyes.clear();
        return "Server has been cleared.";
    }

    public String init() {
        String camName1 = "Tagus";
        Coordinates coordinates1 = Coordinates.newBuilder().setLatitude(38.737613).setLongitude(-9.303164).build();
        String camName2 = "Alameda";
        Coordinates coordinates2 = Coordinates.newBuilder().setLatitude(38.736748).setLongitude(-9.138908).build();

        eyes.put(camName1, coordinates1);
        eyes.put(camName2, coordinates2);

        observations.add(new CarObservation("AA00BB", camName1, new Date()));
        observations.add(new CarObservation("LD04BY", camName2, new Date()));
        observations.add(new PersonObservation(123456, camName1, new Date()));
        observations.add(new CarObservation("4502GS", camName1, new Date()));
        observations.add(new PersonObservation(654321, camName2, new Date()));
        observations.add(new CarObservation("AA43BY", camName2, new Date()));
        observations.add(new PersonObservation(2568628, camName1, new Date()));
        observations.add(new PersonObservation(12344321, camName2, new Date()));

        return "Observations added.";
    }
}
