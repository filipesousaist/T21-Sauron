package pt.tecnico.sauron.eye.domain;

import pt.tecnico.sauron.silo.grpc.Silo.ObjectData;
import pt.tecnico.sauron.silo.grpc.Silo.ObjectType;

public abstract class ObservedObject {
    private ObjectType type;

    void setType(ObjectType type) {
        this.type = type;
    }

    public ObjectType getType() {
        return this.type;
    }

    public abstract ObjectData toObjectData();
}
