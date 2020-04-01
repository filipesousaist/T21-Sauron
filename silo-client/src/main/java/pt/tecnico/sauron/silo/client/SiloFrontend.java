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

    public EyeInfo camInfo(StringMessage eyeName) {
        return stub.camInfo(eyeName);
    }

    public ReportResponse report(EyeObservation eyeObservation) {
        return stub.report(eyeObservation);
    }

    public StringMessage ctrlPing(StringMessage request){ return stub.ctrlPing(request); }

    public StringMessage ctrlClear(EmptyMessage request){ return stub.ctrlClear(request); }

    public StringMessage ctrlInit(EmptyMessage request){ return stub.ctrlInit(request); }

    public ObservationResponse track(ObjectData objectData){
        return stub.track(objectData);
    }

    public ObservationResponse trace(ObjectData objectData){
        return stub.trace(objectData);
    }

    public ObservationResponse trackMatch(ObjectData objectData){
        return stub.trackMatch(objectData);
    }

    @Override
    public void close(){
        channel.shutdown();
    }
}
