package pt.tecnico.sauron.eye.domain;

import pt.tecnico.sauron.silo.grpc.Silo.ObjectData;
import pt.tecnico.sauron.silo.grpc.Silo.ObjectType;

public class ObservedPerson extends ObservedObject {
    private long id;

    ObservedPerson(long id) {
        setType(ObjectType.PERSON);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public ObjectData toObjectData() {
        return ObjectData.newBuilder()
                .setType(getType())
                .setId(Long.toString(id))
                .build();
    }
}
