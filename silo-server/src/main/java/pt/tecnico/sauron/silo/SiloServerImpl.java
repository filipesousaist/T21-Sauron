package pt.tecnico.sauron.silo;


import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.domain.exception.NoObservationMatchExcpetion;
import pt.tecnico.sauron.silo.domain.exception.ObservationNotFoundException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

import java.util.LinkedList;
import java.util.List;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {

    private SiloServerApp siloServerApp = new SiloServerApp();

    private SiloServer siloServer = new SiloServer();

    @Override
    public void ctrlPing(StringMessage request, StreamObserver<StringMessage> responseObserver) {
        String msg = siloServer.ping(request.getText());
        StringMessage response = StringMessage.newBuilder().setText(msg).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(EmptyMessage request, StreamObserver<StringMessage> responseObserver) {
        String msg  = siloServer.clear();
        StringMessage clearResponse = StringMessage.newBuilder().setText(msg).build();

        responseObserver.onNext(clearResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(EmptyMessage request, StreamObserver<StringMessage> responseObserver) {
        String msg  = siloServer.init();
        StringMessage clearResponse = StringMessage.newBuilder().setText(msg).build();

        responseObserver.onNext(clearResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void camJoin(EyeJoinRequest request, StreamObserver<EyeJoinResponse> responseObserver) {
        EyeJoinResponse response = EyeJoinResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void track(ObjectData request, StreamObserver<ObservationResponse> responseObserver) {
        try {
            List<Observation> observations = new LinkedList<>();
            observations.add(siloServer.track(request.getId(), request.getType()));

            ObservationResponse response = buildObservationResponse(observations);

            responseObserver.onNext(response);

            responseObserver.onCompleted();

        }catch (ObservationNotFoundException e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void trackMatch(ObjectData request, StreamObserver<ObservationResponse> responseObserver) {
        try{
            List<Observation> observations = siloServer.trackMatch(request.getId(), request.getType());

            ObservationResponse response = buildObservationResponse(observations);

            responseObserver.onNext(response);

            responseObserver.onCompleted();

        }catch (NoObservationMatchExcpetion e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }



    private ObservationResponse buildObservationResponse(List<Observation> observations){
        ObservationResponse.Builder builder = ObservationResponse.newBuilder();
        List<ObservationData> observationDataList = new LinkedList<>();

        for(Observation obs : observations) {

            Timestamp ts = Timestamp.newBuilder().setSeconds(obs.getDate().getTime()/1000).setNanos(0).build();

            ObservationData observationData = ObservationData.newBuilder()
                    .setType(obs.getType())
                    .setId(obs.getStrId())
                    .setTimestamp(ts)// need to figure out how to build the protobuf timestamp
                    .build();
            // TODO add the camName set,when the cam part is implemented

            observationDataList.add(observationData);
        }
        builder.addAllData(observationDataList);
        return builder.build();
    }
}
