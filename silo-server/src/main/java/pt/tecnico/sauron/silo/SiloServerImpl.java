package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {

    private SiloServerApp siloServerApp = new SiloServerApp();

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

    private SiloServer siloServer = new SiloServer();

    /*@Override
    public void track(Silo.ObjectRequest request, StreamObserver<Silo.ObsResponse> responseObserver) {
        Observation observation = silo.getObservation(request.getId(), request.getType());
        Silo.ObservationResponse response = Silo.ObservationResponse.newBuilder().setId
    }*/

    @Override
    public void ctrlClear(EmptyMessage request, StreamObserver<ClearResponse> responseObserver) {

        String serverStatus = siloServer.clear();
        ClearResponse response = ClearResponse.newBuilder().setClearStatus(serverStatus).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }

}
