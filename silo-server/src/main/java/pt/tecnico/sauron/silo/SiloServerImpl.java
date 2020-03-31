package pt.tecnico.sauron.silo;


import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.domain.exception.NoObservationMatchExcpetion;
import pt.tecnico.sauron.silo.domain.exception.ObservationNotFoundException;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

import com.google.protobuf.Timestamp;

import java.util.ArrayList;
import java.util.LinkedList;
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

            List<Observation> observations = new LinkedList<>();
            observations.add(observation);

            Silo.ObservationResponse response = buildObservationResponse(observations);

            responseObserver.onNext(response);

            responseObserver.onCompleted();

        }catch (ObservationNotFoundException e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void trackMatch(Silo.ObjectData request, StreamObserver<Silo.ObservationResponse> responseObserver) {
        try{
            List<Observation> observations = siloServer.trackMatch(request.getId(), request.getType());

            Silo.ObservationResponse response = buildObservationResponse(observations);

            responseObserver.onNext(response);

            responseObserver.onCompleted();

        }catch (NoObservationMatchExcpetion e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }



    private Silo.ObservationResponse buildObservationResponse(List<Observation> observations){
        Silo.ObservationResponse.Builder builder = Silo.ObservationResponse.newBuilder();
        List<Silo.ObservationData> observationDataList = new LinkedList<>();

        for(Observation obs : observations) {

            Timestamp.Builder ts = Timestamp.newBuilder().setSeconds(obs.getDate().getTime() / 1000).setNanos(0);

            Silo.ObservationData observationData = Silo.ObservationData.newBuilder()
                    .setType(obs.getType())
                    .setId(obs.getStrId())
                    .setDate(ts)// need to figure out how to build the protobuf timestamp
                    .build();
            // TODO add the camName set,when the cam part is implemented

            observationDataList.add(observationData);
        }
        builder.addAllData(observationDataList);
        return builder.build();
    }
}
