package pt.tecnico.sauron.silo;


import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.domain.exception.ObservationNotFoundException;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

import com.google.protobuf.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {

    private SiloServer siloServer = new SiloServer();

    @Override
    public void ctrlClear(Silo.EmptyMessage request, StreamObserver<Silo.ClearResponse> responseObserver) {
        String msg  = siloServer.clear();
        Silo.ClearResponse clearResponse = Silo.ClearResponse.newBuilder().setText(msg).build();

        responseObserver.onNext(clearResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void track(Silo.ObjectData request, StreamObserver<Silo.ObservationResponse> responseObserver) {
        try{
            Observation observation = siloServer.track(request.getId(), request.getType());

            Timestamp.Builder ts = Timestamp.newBuilder().setSeconds(observation.getDate().getTime()/1000).setNanos(0);


            Silo.ObservationData observationData = Silo.ObservationData.newBuilder()
                    .setType(observation.getType())
                    .setId(observation.getStrId())
                    .setDate(ts)// need to figure out how to build the protobuf timestamp
                    .build();
            // TODO add the camName set,when the cam part is implemented


            Silo.ObservationResponse response = Silo.ObservationResponse.newBuilder().setData(0,observationData).build();

            responseObserver.onNext(response);

            responseObserver.onCompleted();

        }catch (ObservationNotFoundException e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
