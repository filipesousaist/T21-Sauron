package pt.tecnico.sauron.eye.domain;

import pt.tecnico.sauron.silo.grpc.Silo.Coordinates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Eye {
    private String name;
    private double latitude;
    private double longitude;
    private List<ObservedObject> observedBuffer;
    private ObservationIterator observationIterator;

    public Eye(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.observedBuffer = new ArrayList<>();
        this.observationIterator = new ObservationIterator();
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return Coordinates.newBuilder()
            .setLatitude(latitude)
            .setLongitude(longitude)
            .build();
    }

    public void addObservedCar(String id) {
        observedBuffer.add(new ObservedCar(id));
    }

    public void addObservedPerson(long id) {
        observedBuffer.add(new ObservedPerson(id));
    }

    public void clearObservations() {
        this.observedBuffer.clear();
        this.observationIterator.reset();
    }

    public boolean hasObservation() {
        return observationIterator.hasNext();
    }

    public ObservedObject getNextObservation() {
        return observationIterator.next();
    }

    private class ObservationIterator implements Iterator<ObservedObject> {
        private int current = 0;

        @Override
        public boolean hasNext() {
            return current < observedBuffer.size();
        }

        @Override
        public ObservedObject next() {
            return observedBuffer.get(current ++);
        }

        public void reset() {
            current = 0;
        }
    }
}
