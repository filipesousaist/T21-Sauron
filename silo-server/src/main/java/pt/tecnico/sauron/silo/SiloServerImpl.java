package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {

    private SiloServer siloServer = new SiloServer();

    /*@Override
    public void track(Silo.ObjectRequest request, StreamObserver<Silo.ObsResponse> responseObserver) {
        Observation observation = silo.getObservation(request.getId(), request.getType());
        Silo.ObservationResponse response = Silo.ObservationResponse.newBuilder().setId
    }*/

    @Override
    public void camJoin(EyeJoinRequest request, StreamObserver<EyeJoinResponse> responseObserver) {
        EyeJoinResponse response = EyeJoinResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }



    /*@Override
    public void ctrlClear(EmptyMessage request, StreamObserver<ClearResponse> responseObserver) {

        String serverStatus = siloServer.clear();
        ClearResponse response = ClearResponse.newBuilder().setClearStatus(serverStatus).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }*/


}
