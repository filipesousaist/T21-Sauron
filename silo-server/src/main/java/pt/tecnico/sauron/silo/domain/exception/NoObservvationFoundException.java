package pt.tecnico.sauron.silo.domain.exception;

import pt.tecnico.sauron.silo.grpc.Silo;

public class NoObservvationFoundException extends Exception{
    public NoObservvationFoundException(String id, Silo.ObjectType type){
        super("No Observations were found whose id matches the expression '"+id+"' and with type:'"+type+"'");
    }

}
