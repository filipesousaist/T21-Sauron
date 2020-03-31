package pt.tecnico.sauron.eye.domain;

import pt.tecnico.sauron.silo.grpc.Silo.ObjectData;
import pt.tecnico.sauron.silo.grpc.Silo.ObjectType;

public class ObservedCar extends ObservedObject {
    private String id;

    ObservedCar(String id) {
        setType(ObjectType.CAR);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public ObjectData toObjectData() {
        return ObjectData.newBuilder()
            .setType(getType())
            .setId(id)
            .build();
    }
}
