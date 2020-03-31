package pt.tecnico.sauron.silo;


import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.domain.exception.ObservationNotFoundException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;


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
            Observation observation = siloServer.track(request.getId(), request.getType());

            Timestamp ts = Timestamp.newBuilder().setSeconds(observation.getDate().getTime()/1000).setNanos(0).build();

            ObservationData observationData = ObservationData.newBuilder()
                    .setType(observation.getType())
                    .setId(observation.getStrId())
                    .setTimestamp(ts) // need to figure out how to build the protobuf timestamp
                    .build();
            // TODO add the camName set,when the cam part is implemented

            ObservationResponse response = ObservationResponse.newBuilder().setData(0,observationData).build();

            responseObserver.onNext(response);

            responseObserver.onCompleted();

        }
        catch (ObservationNotFoundException e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
