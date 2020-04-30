package pt.tecnico.sauron.silo.client;

import java.util.Objects;
import pt.tecnico.sauron.silo.grpc.Silo;

public class SiloCacheKey {

    public enum OperationType {
        SPOT,
        TRAIL,
        CAM_INFO
    }

    public enum ObjectType {
        PERSON,
        CAR,
        NONE
    }

    private OperationType operationType;
    private ObjectType objectType;

    private String argument;

    public SiloCacheKey(OperationType operationType, ObjectType objectType, String argument) {
        this.operationType = operationType;
        this.objectType = objectType;
        this.argument = argument;
    }

    public static ObjectType toObjectType(Silo.ObjectType type){
        switch (type){
            case PERSON:
                return ObjectType.PERSON;
            case CAR:
                return ObjectType.CAR;
            default:
                return ObjectType.NONE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiloCacheKey that = (SiloCacheKey) o;
        return operationType == that.operationType &&
                objectType == that.objectType &&
                Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationType, objectType, argument);
    }
}
