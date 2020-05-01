package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.util.VectorTS;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ObsLog implements Iterable<Observation>{
    private List<Observation> obss = new ArrayList<>();
    private String camName;
    private VectorTS vectorTS;

    public ObsLog(List<ObjectData> data, String camName, Date date, VectorTS vectorTS){
        data.forEach(d -> {
            switch (d.getType()){
                case CAR:
                    obss.add(new CarObservation(d.getId(), camName, date));
                    break;
                case PERSON:
                    obss.add (new PersonObservation(Long.parseLong(d.getId()), camName, date));
                    break;
                default:
                    throw new RuntimeException("Invalid type");
            }
        });


        this.vectorTS = vectorTS;
        this.camName = camName;
    }

    public ObsLog(ObservationLogMessage observationLogMessage){
        this.camName = observationLogMessage.getData(0).getCamName();
        this.vectorTS = new VectorTS(observationLogMessage.getPrevTSMap());
        for(ObservationData of : observationLogMessage.getDataList()){
            Date date = new Date();
            date.setTime(of.getTimestamp().getSeconds()*1000);
            switch (of.getType()){
                case CAR:
                    obss.add(new CarObservation(of.getId(), camName, date));
                    break;
                case PERSON:
                    obss.add (new PersonObservation(Long.parseLong(of.getId()), camName, date));
                    break;
                default:
                    throw new RuntimeException("Invalid type");
            }
        }
    }

    @Override
    public String toString() {
        return obss.toString();
    }

    public List<Observation> getObss() {
        return obss;
    }

    public void setObss(List<Observation> obss) {
        this.obss = obss;
    }

    public VectorTS getVectorTS() {
        return vectorTS;
    }

    public void setVectorTS(VectorTS vectorTS) {
        this.vectorTS = vectorTS;
    }

    public String getCamName() {
        return camName;
    }

    public void setCamName(String camName) {
        this.camName = camName;
    }

    @Override
    public Iterator<Observation> iterator() {
        return obss.iterator();
    }
}
