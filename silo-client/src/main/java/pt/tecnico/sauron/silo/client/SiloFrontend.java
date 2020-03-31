package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc.*;

public class SiloFrontend implements AutoCloseable {
    private final ManagedChannel channel;
    private SiloServiceBlockingStub stub;

    public SiloFrontend(String host, int port){
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        stub = SiloServiceGrpc.newBlockingStub(channel);
    }

    public EyeJoinResponse camJoin(EyeJoinRequest eyeJoinRequest) {
        return stub.camJoin(eyeJoinRequest);
    }

    public Coordinates camInfo(EyeName eyeName) {
        return stub.camInfo(eyeName);
    }

    public ReportResponse report(EyeObservation eyeObservation) {
        return stub.report(eyeObservation);
    }

    //public ClearResponse ctrlClear(EmptyMessage request){ return stub.ctrlClear(request); }

    @Override
    public void close(){
        channel.shutdown();
    }
}
