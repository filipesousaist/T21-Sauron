package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {

    private Silo silo = new Silo();

    @Override
    public void track(Silo.ObjectRequest request, StreamObserver<Silo.ObsResponse> responseObserver) {
        Observation observation = silo.getObservation(request.getId(), request.getType());
        Silo.ObsResponse response = Silo.ObsResponse.newBuilder().setId
    }
}
