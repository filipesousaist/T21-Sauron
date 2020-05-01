package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.util.VectorTS;

public class CamLog {
    private String camName;
    private double latitude;
    private double longitude;
    private VectorTS vectorTS;

    public CamLog(Coordinates coordinates, String cam_name, VectorTS vectorTS) {
        this.latitude = coordinates.getLatitude();
        this.longitude = coordinates.getLongitude();
        this.camName = cam_name;
        this.vectorTS = vectorTS;
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

}
