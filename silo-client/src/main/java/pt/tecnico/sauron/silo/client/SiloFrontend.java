package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc.*;

import java.util.List;

public class SiloFrontend implements AutoCloseable{
    private final ManagedChannel channel;
    private SiloServiceBlockingStub stub;

    public SiloFrontend(String host, int port){
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        stub = SiloServiceGrpc.newBlockingStub(channel);
    }

    public ClearResponse ctrlClear(EmptyMessage request){ return stub.ctrlClear(request); }

    public ObservationResponse track(ObjectData objectData){
        return stub.track(objectData);
    }

    @Override
    public void close(){
        channel.shutdown();
    }
}
