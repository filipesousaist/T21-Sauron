package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {

    private SiloServerApp siloServerApp = new SiloServerApp();

    @Override
    public void spot(ObjectRequest request, StreamObserver<SpotterResponse> responseObserver) {
        String result = siloServerApp.spot(request.getType(), request.getId());
        SpotterResponse response = SpotterResponse.newBuilder().setResult(result).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void trail(ObjectRequest request, StreamObserver<SpotterResponse> responseObserver) {
        String result = siloServerApp.trail(request.getType(), request.getId());
        SpotterResponse response = SpotterResponse.newBuilder().setResult(result).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void ctrlPing(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        PingResponse response = PingResponse.newBuilder().setOutputText(siloServerApp.ctrl_ping()).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(EmptyMessage request, StreamObserver<EmptyMessage> responseObserver) {
        siloServerApp.ctrl_clear();
    }
}
