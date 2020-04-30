package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exception.DuplicateJoinException;
import pt.tecnico.sauron.silo.domain.exception.InvalidCoordinatesException;
import pt.tecnico.sauron.silo.domain.exception.InvalidEyeNameException;
import pt.tecnico.sauron.silo.domain.exception.RepeatedNameException;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.Silo.*;

public class CamLog {
    private String camName;
    private double latitude;
    private double longitude;
    private int opId;

    public CamLog(Coordinates coordinates, String cam_name, int opId) throws InvalidCoordinatesException, DuplicateJoinException, RepeatedNameException {
        double latitude = coordinates.getLatitude();
        double longitude = coordinates.getLongitude();
        if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180)
            throw new InvalidCoordinatesException();

        this.latitude = latitude;
        this.longitude = longitude;
        this.camName = cam_name;
        this.opId = opId;
    }

    public String getCamName() {
        return camName;
    }

    public void setCamName(String camName) {
        this.camName = camName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getOpId() {
        return opId;
    }

    public void setOpId(int opId) {
        this.opId = opId;
    }
}
